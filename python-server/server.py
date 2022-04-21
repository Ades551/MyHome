#! /usr/local/bin/python3.8

# Author: Adam Rajko
# Python 3.8.5

from os import read
from libs import config
from libs import array
import socket
from select import select
from datetime import datetime as date
from time import sleep
from threading import Thread
from serial import Serial
import traceback


def receivedData(received: str, connection):
    '''Communication switch'''
    received = received.split(',')
    # get garden states
    if received[0] == inputs['getGarden']: reply(connection, getStates(garden))
    # get pool states
    elif received[0] == inputs['getPool']: reply(connection, getStates(pool))
    # get time garden
    elif received[0] == inputs['getTimeGarden'][0]: reply(connection, getTiming(received, timing_garden))
    # get time pool
    elif received[0] == inputs['getTimePool'][0]: reply(connection, getTiming(received, timing_pool))
    # get room states
    elif received[0] == inputs['getRoom']: reply(connection, getStates(room))
    # get led states
    elif received[0] == inputs['getOutsideLed']: reply(connection, getStates(led))
    # set states for garden
    elif received[0] == inputs['setGarden'][0]: setState(received, garden)
    # set states for pool
    elif received[0] == inputs['setPool'][0]: setState(received, pool)
    # add new time garden
    elif received[0] == inputs['setTimeGarden'][0]:
        if received[1] == 'all': setTimeSmart(received, timing_garden)
        else: setTime(received, timing_garden)
    elif received[0] == inputs['setTimePool'][0]:
        if received[1] == 'all': setTimeSmart(received, timing_pool)
        else: setTime(received, timing_pool)
    # set room states
    elif received[0] == inputs['setRoom'][0]:
        setState(received, room)
        sendTo('192.168.1.110', 5050, getStates(room))
    # set led state
    elif received[0] == inputs['setOutsideLed'][0]: setState(received, led)
    # remove time garden
    elif received[0] == inputs['removeTimeGarden'][0]: removeTime(received, timing_garden)
    # remove time pool
    elif received[0] == inputs['removeTimePool'][0]: removeTime(received, timing_pool)


def errorMsg(file_name: str):
    '''Prints traceback to error file'''
    date_time = date.now().strftime("%d/%m/%Y, %H:%M:%S")
    message = f'{date_time} - Exception:\n{traceback.format_exc()}\n'
    open(file_name, 'a+').write(message)


def sendTo(addr: str, port: int, message: str):
    '''Send message to some address'''
    client = socket.socket()
    client.connect((addr, port))
    client.send(message.encode())
    if client.recv(1024): client.close()


def sendStatus(message: str):
    '''Send garden states'''
    serial_port.write(message.encode())


def serverTrans():
    while timing_run:
        sendStatus(f'garden{getStates(garden)[1:]}')
        sleep(4) # min 3s
        sendStatus(f'pool{getStates(pool)[1:]}')
        sleep(2) # min 1.5s
        sendStatus(f'led,{getStates(led)}')
        sleep(2) # min .7s


def serverTiming(obj: array.ArrayTime, state: array.ArrayState, full: bool):
    while timing_run:
        if state.get()[0] == 1:
            day = days_en.index(date.now().strftime('%a'))
            hour = date.now().hour
            minute = date.now().minute
            for port in range(obj.getLen()):
                for port_time in obj.get(port):
                    if not full:
                        # will turn on only once per port and one port only (while cycle)
                        if day == port_time[0] and hour == port_time[1] and minute == port_time[2]:
                            state.setState(port + 1, 1)
                            while hour != port_time[3] or minute != port_time[4]:
                                if state.get()[0] == 0: break
                                hour = date.now().hour
                                minute = date.now().minute
                                sleep(2)
                            state.setState(port + 1, 0)
                    else:
                        # will turn on if time is between
                        if day == port_time[0] and (port_time[3] >= hour >= port_time[1]) and ((port_time[4] > minute >= port_time[2]) or hour != port_time[3]):
                            state.setState(port + 1, 1)
                            break
                        else:
                            state.setState(port + 1, 0)
            sleep(2)
        else:
            sleep(2)


def reply(connection, message: str):
    '''Reply message to client'''
    connection.send(message.encode())


def setState(data: list, obj: array.ArrayState):
    '''Set states on objects'''
    # [0] -> port, [1] -> state
    x = [int(i) for i in data[1:]]
    obj.setState(x[0], x[1])


def getStates(obj: array.ArrayState) -> str:
    '''Return states from object to string'''
    states = obj.get()

    text = ""

    for i in states:
        text += f'{i},'

    return text[:-1]


def removeTime(data: list, obj: array.ArrayTime):
    '''Removes time group from timing object'''

    # [0] -> port, [1] -> group
    x = [int(i) for i in data[1:]]

    port = x[0] - 1 # received port has wrong indexing
    arr = obj.get(port) # get times for specific port

    arr.sort(key=lambda x: (x[1], x[2], x[3], x[4])) # sort by time
    group = findGroup(arr) # find group for each timing
    removed = [] # times that will be removed

    [removed.append(arr[i]) for i in group[x[1]]] # add from specific group
    [obj.remove(port, removed[i]) for i in range(len(removed))] # remove from database


def getTiming(data: list, obj: array.ArrayTime) -> str:
    '''Returns timing for specific port from timming object'''

    port = int(data[1]) - 1 # received port
    arr = obj.get(port) # get times from that port

    if not arr: return '' # if empty

    arr.sort(key=lambda x: (x[1], x[2], x[3], x[4])) # sort by timimng
    group = findGroup(arr) # find group for each timing

    text = "" # output text

    for i in group:
        for x in i:
            text += f'{days_sk[arr[x][0]]},' # Po,Ut,St,...
        text = text[:-1] # remove last ,
        text += f' {timeToStr(arr[i[0]])}' # add timing 14:25 - 14:30
        
    return text


def timeToStr(array: list) -> str:
    '''Converts time from array to string
    @return string of time
    '''
    hour_start = str(array[1]).zfill(2)
    min_start = str(array[2]).zfill(2)
    hour_end = str(array[3]).zfill(2)
    min_end = str(array[4]).zfill(2)

    return f'{hour_start}:{min_start} - {hour_end}:{min_end};'


def findGroup(array: list) -> list:
    '''Finds common times inside array
    @return 2D array of groups (by index)
    '''
    group = [[]] # return
    index = 0 # starting index
    start = array[0] # starting array (compared)

    for i in array:
        if start[1:] != i[1:]: # not in the same group
            start = i # change compared time
            group.append([]) # add new group
            index+=1
        group[index].append(array.index(i)) # append index to group
    
    return group


def setTimeSmart(data: list, obj: array.ArrayTime):
    '''Sets time for each port'''

    # conf file set time
    # [0] -> day, [1] -> hour, [2] -> min, [3] -> duration
    x = [int(i) for i in data[2:]] # fisrt item in array is not important

    for i in range(obj.getLen()):
        # hour end = hour + (( min + duration ) // 60)
        # min end = ( min + duration ) % 60
        hour_end = x[1] + ((x[2] + x[3]) // 60)
        min_end = (x[2] + x[3]) % 60

        obj.add(i, [x[0], x[1], x[2], hour_end, min_end])

        x[1] = hour_end
        x[2] = min_end


def setTime(data: list, obj: array.ArrayTime):
    '''Sets time for port'''

    # conf file set time
    # [0] -> port, [1] -> day, [2] -> hour, [3] -> min, [4] -> duration
    x = [int(i) for i in data[1:]] # fisrt item in array is not important

    # hour end = hour + (( min + duration ) // 60)
    # min end = ( min + duration ) % 60
    hour_end = x[2] + ((x[3] + x[4]) // 60)
    min_end = (x[3] + x[4]) % 60

    obj.add(x[0] - 1, [x[1], x[2], x[3], hour_end, min_end])


def closeConnections(socket_list: list, server: socket.SocketKind) -> list:
    '''Close every connection from list except main sever
    @return new list
    '''
    for i in socket_list:
        if i is not server:
            #print('Closed:', i)
            i.close()
    socket_list = [server]

    return socket_list


if __name__ == '__main__':
    days_sk = ("Po", "Ut", "St", "Stv", "Pi", "So", "Ne")
    days_en = ("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    # config file
    conf = config.Config('config.conf')
    conf.initConfigs()

    server_connection = conf.get('connection') # data from config file
    files = conf.get('files') # file names from config
    ports = conf.get('ports') # ports from config

    # file names
    garden_file = files['gardenStates']
    pool_file = files['poolStates']
    time_garden_file = files['timingGarden']
    time_pool_file = files['timingPool']
    room_file = files['roomStates']
    led_file = files['ledStates']

    # array sizes
    time_garden_size = ports['gardenSize']
    time_pool_size = ports['poolSize']
    garden_size = ports['gardenSize'] + 1 if ports['autoGarden'] else ports['gardenSize']
    pool_size = ports['poolSize'] + 1 if ports['autoPool'] else ports['poolSize']
    room_size = ports['roomSize'] + 1 if ports['autoRoom'] else ports['roomSize']
    led_size = ports['ledSize'] + 1 if ports['autoLed'] else ports['ledSize']

    inputs = conf.get('communication') # communication commands

    SERVER_ADDR = ''
    server_port = server_connection['port'] # port from config file

    # initialize main objects and run main program
    try:
        garden = array.ArrayState(garden_file, garden_size)
        pool = array.ArrayState(pool_file, pool_size)
        room = array.ArrayState(room_file, room_size)
        led = array.ArrayState(led_file, led_size)
        timing_garden = array.ArrayTime(time_garden_file, time_garden_size)
        timing_pool = array.ArrayTime(time_pool_file, time_pool_size)

        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM) # initialze socket server

        server.bind((SERVER_ADDR, server_port)) # bind to address and port
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) # idk (bind err)
        server.listen() # listen for connections

        socket_list = [server]

        serial_port = Serial('/dev/ttyS0', 9600, writeTimeout=None)

        timing_run = True
        timing = [Thread(target=serverTrans)]

        if ports['autoGarden']:    
            timing.append(Thread(target=serverTiming, args=(timing_garden, garden, False, )))
        if ports['autoPool']:
            timing.append(Thread(target=serverTiming, args=(timing_pool, pool, True, )))

        [i.start() for i in timing]

        while True:
            '''
            read_socket - wait until ready for reading
            write_socket - wait until ready for reading
            except_socket - wait for exception
            '''
            read_socket, write_socket, except_socket = select(socket_list, [], [], 30)

            for notified_socket in read_socket:
                if notified_socket is server:
                    connection, addr = notified_socket.accept() # accept connection
                    #print("Connected:", addr)
                    socket_list.append(connection)
                else:
                    try:
                        received = notified_socket.recv(1024).decode('utf-8') # received data
                    except:
                        received = None
                    
                    if received:
                        #print(received)
                        receivedData(received, notified_socket)
                        socket_list = closeConnections(socket_list, server)
                    
                    # data not received
                    else:
                        if notified_socket in socket_list:
                            socket_list.remove(notified_socket)
                            #print("Closed:", notified_socket)
                            notified_socket.close()
            
            # after timeout close connections
            if not read_socket:
                if len(socket_list) > 1:
                    socket_list = closeConnections(socket_list, server)
    except:
        errorMsg(files['error']) # print err
        closeConnections(socket_list, server) # close every connection
        server.shutdown(socket.SHUT_RDWR) # shutdown sever
        server.close() # close socket server

        timing_run = False
        [i.join() for i in timing]

        serial_port.close()

        exit(0)
