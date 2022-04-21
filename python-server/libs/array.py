# Author: Adam Rajko
# Python 3.8.5

import json


class ArrayTime:
    def __init__(self, file_name: str, size: int) -> None:
        self.size = size
        self.arr = [[] for _ in range(size)]
        self.file_name = file_name

        try:
            self.load()
        except FileNotFoundError:
            self.save()


    def add(self, port: int, array: list):
        '''Add time array to port'''
        if port < len(self.arr) and port >= 0:
            if not self.arr[port].count(array):
                self.arr[port].append(array)
        else:
            print('FileTime Error: Invalid port!')
        
        self.save()


    def remove(self, port: int, array: list):
        '''Remove time array from port'''
        if port < len(self.arr) and port >= 0:
            self.arr[port].remove(array)
        else:
            print('FileTime Error: Invalid port!')
        
        self.save()


    def getLen(self) -> int:
        return len(self.arr)


    def get(self, port: int) -> list:
        return self.arr[port]


    def getAll(self) -> list:
        return self.arr


    def load(self):
        '''Load data from file'''
        file = open(self.file_name, 'r')
        self.arr = json.load(file)
        file.close()


    def save(self):
        '''Save data to file'''
        file = open(self.file_name, 'w')
        json.dump(self.arr, file)
        file.close()


class ArrayState:
    def __init__(self, file_name: str, size: int) -> None:
        self.size = size
        self.arr = [0 for _ in range(size)]
        self.file_name = file_name

        try:
            self.load()
        except FileNotFoundError:
            self.save()


    def setState(self, port: int, state: int):
        '''Set port state'''
        if port < len(self.arr) and port >= 0:
            if state != self.arr[port]:
                self.arr[port] = state
                self.save()


    def get(self) -> list:
        '''Returns states'''
        return self.arr


    def load(self):
        '''Load data from file'''
        file = open(self.file_name, 'r')
        self.arr = json.load(file)
        file.close()


    def save(self):
        '''Save data to file'''
        file = open(self.file_name, 'w')
        json.dump(self.arr, file)
        file.close()
