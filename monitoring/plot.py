import matplotlib.pyplot as plt
import numpy as np


class Plot:
    def __init__(self,rows=1,cols=1):
        self.fig, self.axes = plt.subplots(rows, cols, figsize=(16, 9), sharex=True,squeeze=False)
        print(self.axes)

    def add(self,data, row=0,col=0, x=0, y=1,label='', l='-'):
        data = np.array(data)
        ax= self.axes[row][col]
        ax.plot(data[:,x],data[:,y],l,label=label)
        return self

    def show(self,legend=True):
        if legend:
            for ax in self.axes.flatten():
                print(ax)
                ax.legend()
        plt.show()

