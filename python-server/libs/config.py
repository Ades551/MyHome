# Author: Adam Rajko
# Python 3.8.5


class Config:
    def __init__(self, input_file: str) -> None:
        try:
            self.file = open(input_file, 'r') # open file
        except FileNotFoundError:
            print(f'File {input_file} not found!') # config file not found
            exit(1)

        self.configs = {}


    def initConfigs(self):
        '''Read configs to dict'''
        
        now = None
        for line in self.file.readlines():

            # section start
            if line.count('['):
                now = line.replace('[', '').replace(']', '').strip()
                self.configs[now] = {}

            # add items from section to dict
            elif line != '\n' and now is not None:
                line = line.split('=')

                key = line[0].strip()
                item = line[1].strip()

                if item.count(','): self.configs[now][key] = item.split(',')
                else: self.configs[now][key] = item

        self.configs = self.convertValues(self.configs)
        

    def convertValues(self, x: dict) -> dict:
        '''List throught dict and converts data types'''
        for key, item in x.items():
            # key item is type of dict
            if type(x[key]) == dict:
                x[key] = self.convertValues(x[key]) # recursive call
            elif type(x[key]) == str:
                # convert items (int, bool)
                if item.isnumeric(): x[key] = int(item)
                elif item == 'True': x[key] = True
                elif item == 'False': x[key] = False
        return x
        

    def get(self, key: str):
        '''Returns config (section)'''
        return self.configs[key]


    def getAll(self) -> dict:
        '''Returns config dict'''
        return self.configs
