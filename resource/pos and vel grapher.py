import os.path
import re
import subprocess
from pathlib import Path

import matplotlib.pyplot as plt
import seaborn as sns

path = r'..\demos\cm\14'
graph_velocities = False
# color_palette = plt.get_cmap("tab10").colors
color_palette = sns.color_palette("husl")


def parent_dir(demo):
    return str(Path(demo.demo_file).parent)


# the demos are colored based on the int from this function
def demo_hash_as_int(demo):
    return demo.max_tick
    # return int.from_bytes(parent_dir(demo).encode("utf-8"), byteorder='little')


position_matcher = re.compile(r'\|[0-9]+\|~\|')


class Demo:
    def __init__(self, demo_file):
        self.demo_file = demo_file
        parser_result = subprocess.check_output(
            ("UncraftedDemoParser.exe", demo_file, "-p", "-R"), stderr=subprocess.STDOUT).decode("ascii")
        self.positions = []
        for line in parser_result.split('\n'):
            if position_matcher.match(line):
                self.positions.append(line)
        if len(self.positions) is 0:
            print(parser_result)
            raise FileNotFoundError

        # positions => [demo][tick]
        # every element looks like "|tick|~|x1,y1,z1|pitch1,yaw1,roll1|~|x2,y2,z2|pitch2,yaw2,roll2|..."
        # we want just the tick and positions of player 1, so every element will look like "(tick, (x,y,z))"

        for i in range(len(self.positions)):
            component = self.positions[i].split('|', 4)  # ['', tick, '~', 'x,y,z..']; we only care about elements 1 & 3
            self.positions[i] = int(component[1]), tuple(map(lambda s: float(s), component[3].split(',')))

        # make sure all ticks are unique & positive
        self.positions = list(filter(lambda x: x[0] >= 0, {pos[0]: pos for pos in self.positions}.values()))
        self.positions.sort(key=lambda x: x[0])
        self.max_tick = self.positions[-1][0]

        self.velocities = []
        for i in range(len(self.positions) - 1):
            self.velocities.append((self.positions[i][0], tuple(map(lambda x: (self.positions[i + 1][1][x] - self.positions[i][1][x]) / (self.positions[i + 1][0] - self.positions[i][0]) * 66.66, range(3)))))
        self.velocities.append((self.positions[-1][0], tuple(map(lambda x: (self.positions[-2][1][x] - self.positions[-1][1][x]) / (self.positions[-1][0] - self.positions[-2][0]) * 66.66, range(3)))))


print("loading demos")
demos = []
if os.path.isfile(path):
    demos.append(Demo(path))
else:
    for root, dirs, files in os.walk(path):
        for file in files:
            if file.endswith(".dem"):
                try:
                    demos.append(Demo(os.path.join(root, file)))
                except FileNotFoundError:
                    print("skipping demo " + file)

print(str(len(demos)) + ' demos loaded')
demos.sort(key=lambda x: x.demo_file)

fig, axs = plt.subplots(nrows=3, ncols=2 if graph_velocities else 1, sharex='all')
fig.suptitle("Positions")
plt.xlabel('tick')
color_func = lambda x: color_palette[demo_hash_as_int(x) % len(color_palette)]

if graph_velocities:
    for i, graph_type in enumerate(("pos x", "pos y", "pos z")):
        for demo in demos:
            axs[i, 0].plot(
                [pos[0] for pos in demo.positions],
                [pos[1][i] for pos in demo.positions],
                color=color_func(demo))
            print(demo.demo_file + " colored " + '#%02x%02x%02x' % tuple(map(lambda x: int(x * 256), color_func(demo))))
        axs[i, 0].title.set_text(graph_type)
        axs[i, 0].grid(True)
    for i, graph_type in enumerate(("vel x", "vel y", "vel z")):
        for demo in demos:
            axs[i, 1].plot(
                [vel[0] for vel in demo.velocities],
                [vel[1][i] for vel in demo.velocities],
                color=color_func(demo))
            print(demo.demo_file + " colored " + '#%02x%02x%02x' % tuple(map(lambda x: int(x * 256), color_func(demo))))
        axs[i, 1].title.set_text(graph_type)
        axs[i, 1].grid(True)
else:  # just graph the positions
    for i, graph_type in enumerate(("pos x", "pos y", "pos z")):
        for demo in demos:
            axs[i].plot(
                [pos[0] for pos in demo.positions],
                [pos[1][i] for pos in demo.positions],
                color=color_func(demo))
            print(demo.demo_file + " colored " + '#%02x%02x%02x' % tuple(map(lambda x: int(x * 256), color_func(demo))))
        axs[i].title.set_text(graph_type)
        axs[i].grid(True)
plt.show()
