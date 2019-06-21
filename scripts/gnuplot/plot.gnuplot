
live=0
file="2019-06-21_test-1.log"
my_xtics=120
reset

############ This was for eps
#set terminal postscript enhanced color size 16,12
#set output 'plot.eps'
#######################################

set terminal png size 1280,720
set output 'plot.png'

set multiplot layout 2,1 rowsfirst
set grid


############################################# Plot 1
set title "$$Workload$$"
set xlabel "Time"
set ylabel "Count/s"
set xtics my_xtics
set xdata time
set timefmt "%s"
set format x "%Y-%m-%d %H:%M:%S"
set key default


#set xrange[1559555062: 1559555492]

plot file using 1:2 with lp lt 1 pt 2 ps 0.7  title "Config Requests" , \
     file using 1:3 with lp lt 2 pt 2 ps 0.6  title "Real Requests", \
     file using 1:4 with lp lt 3 pt 2 ps 0.6  title "Throughput"


##################### Plot 2
set title "Workload"
set xlabel "Time"
set ylabel "Duration"
set xdata time
set timefmt "%s"
set xtics my_xtics
set format x "%Y-%m-%d %H:%M:%S"
set key default

#set yrange[0:2000]
#set xrange[1559555062: 1559555492]

plot file using 1:5 with lp lt 1 pt 0 ps 0.4  title "Min Latency" , \
     file using 1:6 with lp lt 2 pt 0 ps 0.6  title "Avg Latency", \
     file using 1:7 with lp lt 3 pt 0 ps 0.6  title "Max Latency"

if(live){
    unset terminal
    set title "s"
    while (1) {
        #unset terminal
        replot
        pause 1
        #clear
    }
}else{



}

