import srcomapi
import srcomapi.datatypes as dt
import re
import requests
import os
from google_drive_downloader import GoogleDriveDownloader as gdd
import urllib.request
import shutil
# from mediafire import MediaFireApi

# this script will take a few minutes to run. if it throws an error, trying running it again lol

download_path = "../../demos/downloaded demos"
min_demo_per_cat_required = 5  # only downloads the demo if there's at least this many demos in a category

excluded_chambers = (("Out of Bounds", "08"), ("Out of Bounds", "14"), ("Out of Bounds", "00-01"),
                     ("Out of Bounds", "09"), ("Out of Bounds", "10"), ("Out of Bounds", "Adv 14"))


def allow_run(run):
    if run["player"] == "InlandEmpireCuber":
        return False
    if (run["category"], run["level"]) in excluded_chambers:
        return False
    return True


api = srcomapi.SpeedrunCom()
game = api.search(dt.Game, {"name": "portal"})[0]

il_runs = {}
print("getting runs...")

for category in game.categories:
    if category.name not in il_runs:
        il_runs[category.name] = {}
    if category.type == 'per-level':
        for level in game.levels:
            leaderboard = dt.Leaderboard(api, data=api.get(
                "leaderboards/{}/level/{}/{}?embed=variables".format(game.id, level.id, category.id)))
            if (category.name, level.name) in excluded_chambers:
                print("skipping il's for {} {}: excluded chamber".format(level.name, category.name))
                continue
            run_count = len(leaderboard.runs)
            if run_count < min_demo_per_cat_required:
                print("skipping il's for {} {}, only {} runs(s)".format(level.name, category.name, run_count))
                continue
            il_runs[category.name][level.name] = leaderboard

link_matcher = re.compile(r"(?P<url>https?://[^\s]+)")
runs = []
print("getting run info...")

# make my own dict that's a bit more convenient to work with
for category in il_runs:
    for level in il_runs[category]:
        for run in il_runs[category][level].runs:
            comment = run["run"].comment
            video = run["run"].videos["links"][0]["uri"] if run["run"].videos is not None else None
            links = set(link_matcher.findall(run["run"].comment) if comment is not None else ())  # empty by default
            if video is not None:
                links.add(video)
            runs.append({
                "category": category,
                "level": level,
                "player": run["run"].players[0].name,
                "comment": comment,
                "links": tuple(links),
                "video": video})


def print_run(run_, message=None):
    print('-'*50)
    if message is not None:
        print(message)
        print('-'*50)
    for key in sorted(run_.keys()):
        print("{}: {}".format(key, run_[key]))


# def download_mediafire(link, file_location):
#     response = MediaFireApi().file_get_links(next(x for x in link.split('/') if len(x) is 15))  # get quick key
#     normal_download_url = response['links'][0]['normal_download']
#     response = requests.get(normal_download_url, stream=True)
#     with io.open(file_location, 'wb') as fd:
#         for chunk in response.iter_content(chunk_size=4096):
#             fd.write(chunk)


def download_googledrive(link, file_location):
    split_str = re.split("id=|/|&", link)
    gdd.download_file_from_google_drive(
        file_id=next(x for x in split_str if x is not None and (len(x) is 33 or len(x) is 28)),  # quick key
        dest_path=file_location,
        unzip=True)


def download_direct(link, file_location):
    r = requests.get(link, stream=True)
    with open(file_location, 'wb') as out_file:
        shutil.copyfileobj(r.raw, out_file)
    # u = urllib.request.urlopen(link)
    # data = u.read()
    # u.close()
    # with open(file_location, "wb") as f:
    #     f.write(data)


def download_dropbox(link, file_location):
    if link[-4:] == ".dem":
        link += "?dl=1"
    elif link[-5:] == "?dl=0":
        link = link[:-5] + "?dl=1"
    download_direct(link, file_location)


def download_onedrive(link, file_location):
    link = link.replace("https://1drv.ms", "https://1drv.ws")
    u = urllib.request.urlopen(link)
    data = u.read()
    u.close()
    with open(file_location, "wb") as f:
        f.write(data)


download_discord = download_direct
download_googledocs = download_googledrive


def get_extension(link):
    tmp = "." + re.split("[^a-zA-Z]", link.split(".")[-1])[0]
    return tmp if tmp != ".com" else ""


if not os.path.exists(download_path):
    os.makedirs(download_path)

re_db = re.compile(r"https?://www\.dropbox\.com"),              "dropbox"
re_gd = re.compile(r"https?://drive\.google\.com"),             "googledrive"
re_dc = re.compile(r"https?://cdn\.discordapp\.com"),           "discord"
re_gc = re.compile(r"https?://docs\.google\.com"),              "googledocs"
re_od = re.compile(r"https?://1drv\.ms"),                       "onedrive"

# not download links
re_puush = re.compile(r"https?://puu\.sh"),                     "puush"
re_yt = re.compile(r"https?://(www\.youtube\.com|youtu\.be)"),  "youtube"
re_tw = re.compile(r"https?://www\.twitch\.tv"),                "twitch"

# can't automate, display for user
re_mf = re.compile(r"https?://www\.mediafire\.com"),            "mediafire"
re_mg = re.compile(r"https?://mega(\.co)?\.nz"),                "mega"

links_to_print = []
links_to_download = []
misc_links_to_print = []  # youtube or twitch, might contain links in the description

print("filtering links...")

for run in runs:

    if not allow_run(run):
        print_run(run, "filtered out")
        continue

    # get all the links which match any of the above regex's, the dict will look like this:
    # {"dropbox": (link1, link2), "discord": (link1,)...}
    working_links = {re_site[1]: tuple(filter(lambda link: re_site[0].match(link), run["links"]))
                     for re_site in (re_db, re_gd, re_dc, re_gc, re_od, re_mf, re_mg, re_yt, re_tw, re_puush)}
    # check for 404
    working_links = {site: tuple(filter(lambda link: requests.get(link).status_code is not 404, links))
                     for site, links in working_links.items()}

    # get any youtube and twitch links, save to separate dict
    misc_links = {site: links for site, links in working_links.items() if len(links) > 0
                  and site in ("youtube", "twitch")}

    # filter original dict for any direct links (not puush, youtube, or twitch)
    working_links = {site: links for site, links in working_links.items() if len(links) > 0
                     and site not in ("puush", "youtube", "twitch")}

    if len(working_links) is 0:
        if len(misc_links) is 0:
            print_run(run, "no usable links found")
            continue
        # if no direct links but has youtube or twitch; might have links in the description
        misc_links_to_print.append(tuple(misc_links.items())[0])
        continue

    # there's more than 1 direct link, i don't want to download both so i'll print that
    if any(map(lambda links: len(links) > 1, working_links.values())) or len(working_links) > 1:
        links_to_print.append(tuple(working_links.items())[0])
        continue

    # mega or mediafire link, can't automatically download
    if any(map(lambda re_site: re_site[1] in working_links.keys(), (re_mf, re_mg))):
        links_to_print.append(tuple(working_links.items())[0])
        continue

    links_to_download.append((run, tuple(working_links.items())[0]))


links_to_download = list(map(lambda l: (l[0], (l[1][0], l[1][1][0])), links_to_download))
print("downloading to {}...".format(os.path.abspath(download_path)))

for link in links_to_download:  # [(run1 ("site1", "link1")), (run2, ("site2", "link2"))...]

    # noinspection PyTypeChecker
    full_path = os.path.join(os.path.abspath(download_path), "{} {} {}{}".format(
        link[0]["level"], link[0]["category"], link[0]["player"], get_extension(link[1][1])))

    print("{}downloading {}".format("not sure of extension, " if "." not in full_path else "", link[1][1]))

    if link[1][0] == "dropbox":
        download_dropbox(link[1][1], full_path)
    elif link[1][0] == "googledrive":
        download_googledocs(link[1][1], full_path)
    elif link[1][0] == "discord":
        download_discord(link[1][1], full_path)
    elif link[1][0] == "googledocs":
        download_googledocs(link[1][1], full_path)
    elif link[1][0] == "onedrive":
        download_onedrive(link[1][1], full_path)

# set extensions for any downloaded files which we believe are demo files
for d in os.listdir(download_path):
    full_path = os.path.join(download_path, d)
    if d[-4:] != ".dem":
        with open(full_path, 'rb') as f:
            if f.read(8) != b'HL2DEMO\x00':  # demo "header"
                continue
        print("setting file extension for " + d)
        os.rename(full_path, full_path + ".dem")

# sort by site
misc_links_to_print.sort(key=lambda l: l[0])
misc_links_to_print = list(map(lambda l: l[1], misc_links_to_print))
links_to_print.sort(key=lambda l: l[0])
links_to_print = list(map(lambda l: l[1], links_to_print))

print(('-' * 50) + "\nthese might have links to demos in the description\n" + ('-' * 50))
print(*misc_links_to_print, sep='\n')
print(('-' * 50) + "\nthe following links could not be downloaded automatically\n" + ('-' * 50))
print(*links_to_print, sep='\n')
