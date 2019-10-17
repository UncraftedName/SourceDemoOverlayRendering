package playerInfo;

public enum PlayerIcon {

    // if the path is not present or null, it's set to a default path

    Imanex("https://yt3.ggpht.com/a/AGF-l78xn13f4gdNE1RH07wP0s6I3LbgblAitRQY4w"),
    UncraftedName("https://cdn.discordapp.com/avatars/396934998881730560/b37dbe83405b45a1bbf7da61be937db4.png?size=512"),
    Jonah("https://cdn.discordapp.com/avatars/109283823866368000/866b12ed65e535494f88b59e08358447.png?size=512"),
    LamboLord24("https://cdn.discordapp.com/avatars/164439274761093120/18ddc423072141c59b4c3a96e87bfb6b.png?size=512"),
    BiiWix("https://cdn.discordapp.com/avatars/95888044799827968/9a3eddc26aa6c61eaeb2cb9ecf5bbec1.png?size=512"),
    Fnzzy("img/icons/players/Fnzzy (⁎^ᴗ^⁎).png"),
    Skizzie("https://cdn.discordapp.com/avatars/126044481681227776/e933b79cfa2a6ba5a00be88c5c1ddb6c.webp?size=512"),
    michael94("https://cdn.discordapp.com/avatars/128559725951451136/6a2d19deb35e078867841bcb03017a50.webp?size=512"),
    Hoshi("https://cdn.discordapp.com/avatars/161540570076938240/b1d09d144bb2ba000ca51f3cbf0c29ed.webp?size=512"),
    Hoshi_Inu("https://cdn.discordapp.com/avatars/161540570076938240/b1d09d144bb2ba000ca51f3cbf0c29ed.webp?size=512"),
    kojlax("https://cdn.discordapp.com/avatars/159819497216278529/85a2e6752c5c6a99572c04c16fcecb8d.png?size=512"),
    No_Ire_Cat("https://cdn.discordapp.com/avatars/83025241139318784/ac0328c2f6b95f4687c14283c551975c.png?size=512"),
    Toad("https://cdn.discordapp.com/avatars/90511350039601152/b499156092e0f3eefb27cc87a766f540.png?size=512"),
    NuSa("https://cdn.discordapp.com/avatars/173839555303178240/cad99646a97c59bddc5fda543e9779a3.webp?size=512"),
    Pr0tal_Player("https://cdn.discordapp.com/avatars/251989047709270018/311306106039ef97e16f164d5150eff3.png?size=512"),
    maltemller("https://cdn.discordapp.com/avatars/286997284422418433/4e07d866652526a0e8f62b22aa3a6565.webp?size=512"),
    XeiZ("https://cdn.discordapp.com/avatars/84334020552302592/f5cca5a0f050ede48d47e13c942d96ac.webp?size=512"),
    HellSt0ne("https://discordapp.com/assets/dd4dbc0016779df1378e7812eabaa04d.png"),
    Avetixz("https://cdn.discordapp.com/avatars/82983885075390464/da714bfa1acd7c3a2043447e66f22460.png?size=512"),
    Dacciox("https://cdn.discordapp.com/avatars/82980713455943680/7f739ce62bb7b0c7df75ae85011f607b.png?size=512"),
    Tendersteel("https://cdn.discordapp.com/avatars/188838860267061249/2d3a227029a3810105b20d1deebc9ec4.webp?size=512"),
    eljest("https://cdn.discordapp.com/avatars/450725660013363210/fb55c9aff3d1b4f1463f548db42dda37.webp?size=512"),
    adamantite("https://cdn.discordapp.com/avatars/138793454485504000/5a27cd07716e215b05a3723fc5824930.png?size=512"),
    Zeta("https://cdn.discordapp.com/avatars/168895166143397888/fe384f385ce70c8510e21080def91336.webp?size=512"),
    sunsetbear("https://cdn.discordapp.com/avatars/108674374172721152/3570ab92b70c3eed9f75f8042003bff9.webp?size=512"),
    Imita("https://cdn.discordapp.com/avatars/155730575183970304/9f953f7af4d7dfd73d6af936b62fd987.webp?size=512"),
    KnockKnock("https://cdn.discordapp.com/avatars/208801815662166018/f20a7e042d697efd186e77137337de43.png?size=512"),
    Goodigo("https://cdn.discordapp.com/avatars/82861107223465984/aed7a82d4f925c5310c91e5dda5edcf2.png?size=512"),
    Blizik("https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/42/421ded019c1743cea1f1f078b0ca5e7d9bc3d848_full.jpg"),
    moose_hockey("img/icons/players/moose.hockey.63.png"),
    Dinoz("https://cdn.discordapp.com/avatars/227596957835591681/359a1f80ecc85715cfd44a76771559e6.webp?size=512"),
    BANANA_BOYE("https://cdn.discordapp.com/avatars/156921823341051904/e9dc8657e70438541713eab80e8a7574.webp?size=512"),
    FluffyGMD("https://cdn.discordapp.com/avatars/289964246643769344/09d0785fecd0bbaca40c91e78726d4d2.webp?size=512"),
    calamity("https://cdn.discordapp.com/avatars/317041055688163338/25c80d3b9ce6288062f4ade8bbc8623d.webp?size=512");


    public static final String DEFAULT_URI = "img/icons/sanic.png";
    public final String uri;

    PlayerIcon(final String uri) {
        if (uri == null)
            this.uri = DEFAULT_URI;
        else
            this.uri = uri.replace(".webp", ".png");
    }

    PlayerIcon() {
        this(null);
    }
}
