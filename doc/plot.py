#!/usr/bin/python3
import matplotlib.pyplot as plt
import numpy as np

d = np.genfromtxt("/home/zgeorg03/git-projects/Workload-Generator/2018-07-09_test.log",delimiter="\t")
fig,(ax1,ax2) = plt.subplots(2,1,figsize=(16,9))
d[:,0] = (d[:,0] - d[0][0]) /1000

ax1.set(title="# Threads=80, 1 Geoghash Request")
ax1.plot(d[:,0],d[:,1],"-+",label="ConfiguredRPS")
ax1.plot(d[:,0],d[:,2],"-+",label="Real RPS")
ax1.plot(d[:,0],d[:,3],"-+",label="Throughput")
ax1.grid()
ax1.legend()



ax2.plot(d[:,0],d[:,4],"-+",label="Avg Latency")
ax2.plot(d[:,0],d[:,5],"-+",label="Min Latency")
ax2.plot(d[:,0],d[:,6],"-+",label="Max Latency")

ax2.set(ylabel="Duration (ms)",xlabel="Time (s)")
ax2.grid()
ax2.legend()
plt.show()