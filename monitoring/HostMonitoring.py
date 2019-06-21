from datetime import datetime as dt
import collections
import dateutil.parser as dp
import requests
import time
from plot import Plot


class HostMonitor:
    api_stats = "/api/v2.0/stats?type=docker&recursive=true"

    def __init__(self, host="localhost", port=8080):
        self.host = host
        self.port = port
        self.last_timestamp = None

    def call_stats(self):
        if self.last_timestamp:
            count = 4
        else:
            count = 64
        response = requests.get("http://{}:{}{}&count={}".format(self.host, self.port, HostMonitor.api_stats,count))
        if response.status_code != 200:
            print(response.status_code)
            return
        containers = response.json()
        data = {}
        for container in containers:
            split = container.strip().split("/")
            if len(split) < 3: continue
            id = split[2]
            parse_container(containers[container], id, data)
            #if id == "2e0c82b0a332a470cd82496b6f3051d3a7e1bf7f95e8f947be82b018a214d874":
            #    p = Plot(2, 1)
            #    p.add(container_info['cpu_utilization'], 0, 0, label="CPU Utilization")
            #    p.add(container_info['memory'], 1, 0, label="Memory Usage")
            #    p.add(container_info['memory'], 1, 0, x=0, y=2, label="Memory Capacity")
            #    # p.show()

        sort = collections.OrderedDict(sorted(data.items(), reverse=False))
        ## Check last time stamp rform file
        if self.last_timestamp:
            filtered = {k: v for k, v in sort.items() if k > self.last_timestamp}
            self.push_to_file("test.log", filtered)
        else:
            self.push_to_file("test.log", sort)

    def push_to_file(self, file, data):
        with open(file, 'a') as fp:
            for timestamp in data:
                fp.write("{}".format(timestamp))
                for k, v in data[timestamp]:
                    fp.write("\t{}:{}".format(k, v))
                fp.write("\n")
                # records = data[timestamp].join("#")
                self.last_timestamp = timestamp

def parse_container(records, container, data):
    result = {}
    cpu_result = []
    mem_result = []
    for record in records:
        timestamp = int(dt.timestamp(dp.parse(record['timestamp'])) * 1000)
        if timestamp not in data:
            data[timestamp] = []

        if record['has_cpu']:
            cpu_usage = record['cpu']['usage']['total']
            cpu_result.append((timestamp, cpu_usage))

        if record['has_memory']:
            memory_usage = record['memory']['usage']
            memory_capacity = record['memory']['max_usage']
            mem_result.append((timestamp, memory_usage, memory_capacity))

            data[timestamp].append(("{}:{}".format(container, "memory_usage"), memory_usage))
            data[timestamp].append(("{}:{}".format(container, "memory_capacity"), memory_capacity))

    cpu_utilization = convert_to_time_utilization(cpu_result)
    for timestamp, value in cpu_utilization:
        data[timestamp].append(("{}:{}".format(container, "cpu_usage"), value))

    result['cpu_utilization'] = cpu_utilization
    result['memory'] = mem_result
    return result


def convert_to_time_utilization(raw):
    utilization = []
    last_timestamp = None
    last_value = None
    for data in raw:
        timestamp, value = data
        if not last_timestamp:
            last_timestamp = timestamp
            last_value = value
        else:
            interval = (timestamp - last_timestamp) * 1000000  # To ns
            utilization.append((timestamp, (value - last_value) / interval))
            last_timestamp = timestamp
            last_value = value
    return utilization


if __name__ == '__main__':
    monitor = HostMonitor(host="10.16.3.38", port=8080)
    while True:
        monitor.call_stats()
        time.sleep(5)
