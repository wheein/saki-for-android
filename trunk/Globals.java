package mahjong.riichi;

public class Globals {
	//ID numbers
	//public static int SCORESCREEN = 1;
	
	//Others
	public static String VERSION = "v1.0.0 Release Candidate";
	
	public static int SIMPLE = 1;
	public static int TERMINAL = 2;
	public static int HONOR = 3;
	
	public static int MAXTILE = 34; //Depreciated, use Tile.LAST_TILE
	
	//Yaku
	public static int PINFU = 0; 
	public static int TANYAO = 1; 
	public static int IIPEIKOU = 2; 
	public static int YAKUHAI = 3; 
	public static int SANSHOKUDOUJUN = 4; 
	public static int ITSU = 5; 
	public static int CHANTA = 6; 
	public static int HONROUTOU = 7; 
	public static int TOITOI = 8; 
	public static int SANANKOU = 9; 
	public static int SANKANTSU = 10; 
	public static int SANSHOKUDOUKOU = 11; 
	public static int CHIITOI = 12; 
	public static int SHOUSANGEN = 13; 
	public static int HONITSU = 14; 
	public static int JUNCHANTAYAO = 15; 
	public static int RYANPEIKOU = 16; 
	public static int CHINITSU = 17; 
	
	//These are the only Yaku that the AI needs to worry about
	public static int AIYAKUCOUNT = 18;
	
	//These are the rest
	public static int MENZEN = 18;
	public static int RIICHI = 19;
	public static int IPPATSU = 20;
	public static int DOUBLERIICHI = 21;
	public static int HAITEI = 22;
	public static int HOUTEI = 23;
	public static int RINSHAN = 24;
	public static int CHANKAN = 25;
	public static int NAGASHIMANGAN = 26;
	
	public static int NONYAKUMAN = 27;
	
	//Yakuman...stupid yakuman
	public static int KOKUSHUMUSOU = 27;
	public static int DAISANGEN = 28;
	public static int SHOUSUUSHII = 29;
	public static int DAISUUSHII = 30;
	public static int CHUURENPOUTOU = 31;
	public static int SUUANKOU = 32;
	public static int RYUUIISOU = 33;
	public static int SUUKANTSU = 34;
	public static int TSUUIISOU = 35;
	public static int CHINROUTOU = 36;
	public static int TENHOU = 37;
	public static int CHIIHOU = 38;
	
	public static int ALLYAKUCOUNT = 39;
	
	//Tile Classification
	public static class Suits{
		public static int BAMBOO = 1;
		public static int PIN = 2;
		public static int MAN = 3;
		public static int SANGEN = 4;
		public static int KAZE = 5;
	}
	
	//Winds
	public static class Winds{
		public static int EAST = 0;
		public static int SOUTH = 1;
		public static int WEST = 2;
		public static int NORTH = 3;
	}
	
	//Commands
	public static class CMD{
		public static int PASS = 0;
		public static int TSUMO = 100;
		public static int RON = 600;
		public static int PON = 400;
		public static int KAN = 500;
		public static int CHI = 300;
		public static int SELFKAN = 200;
		public static int RIICHI = 201;
		public static int TENPAI = 1000;
		public static int NOTEN = 1001;
	}
	
	public static class Powers{
		/**
		 * The drawBased powers will increase the odds of drawing what is in your
		 * powerTiles list.
		 * drawBased = 100% chance as long as it's possible
		 * DoubleOdds will redraw a second time if it doesn;t come up the first time
		 * TripleOdds is the same thing but 2 additional draws
		 * 
		 * Invisibility is a Momoka specific power
		 */
		public static int drawBased = 0;
		public static int invisibility = 1;
		public static int drawBased_DoubleOdds = 2;
		public static int drawBased_TripleOdds = 3;
		public static int pureVision = 4;
		public static int dealBased = 5;
		public static int COUNT = 6;
		
		
	}
	
	//Character IDs
	public static class Characters{
		//public static int GENERIC = 0;
		public static int SAKI = 0;
		public static int NODOKA = 1;
		public static int YUUKI = 2;
		public static int MAKO = 3;
		public static int HISA = 4;
		public static int TOUKA = 5;
		public static int KOROMO = 6;
		public static int HAJIME = 7;
		public static int TOMOKI = 8;
		public static int JUN = 9;
		public static int MIHOKO = 10;
		public static int KANA = 11;
		public static int MIHARU = 12;
		public static int SEIKA = 13;
		public static int SUMIYO = 14;
		public static int YUMI = 15;
		public static int MOMOKA = 16;
		public static int SATOMI = 17;
		public static int KAORI = 18;
		public static int MUTSUKI = 19;
		
		public static int COUNT = 20;
		
		public static class Graphics{
			public static int NEUTRAL = 0;
			public static int HAPPY = 1;
			public static int SAD = 2;
			public static int FULL = 3;
			
			public static int COUNT = 4;
		}
		
		public static String getName(int CharID, boolean bJapanese){
			if(CharID == SAKI){
				if(bJapanese)
					return "宮永 咲 (みやなが　さき)";
				return "Miyanaga Saki";
			}
			else if(CharID == KANA){
				if(bJapanese)
					return "池田華菜 (いけだ　かな)";
				return "Ikeda Kana";
			}
			else if(CharID == KOROMO){
				if(bJapanese)
					return "天江 衣 (あまえ　ころも)";
				return "Amae Koromo";
			}
			else if(CharID == MOMOKA){
				if(bJapanese)
					return "東横桃子(とうよこ　ももか)";
				return "Touyoko Momoka";
			}
			else if(CharID == NODOKA){
				if(bJapanese)
					return "原村 和 (はらむら　のどか)";
				return "Haramura Nodoka";
			}
			else if(CharID == YUUKI){
				if(bJapanese)
					return "片岡 優希 (かたおか　ゆうき)";
				return "Kataoka Yuuki";
			}
			else if(CharID == MAKO){
				if(bJapanese)
					return "染谷 まこ(そめや　まこ)";
				return "Someya Mako";
			}
			else if(CharID == HISA){
				if(bJapanese)
					return "竹井 久(たけい　ひさ)";
				return "Takei Hisa";
			}
			else if(CharID == TOUKA){
				if(bJapanese)
					return "龍門渕透華 (りゅうもんぶち　とうか)";
				return "Ryuumonbuchi Touka";
			}
			else if(CharID == HAJIME){
				if(bJapanese)
					return "国広一 (くにひろ　はじめ)";
				return "Kunihiro Hajime";
			}
			else if(CharID == TOMOKI){
				if(bJapanese)
					return "沢村智紀(さわむら　ともき)";
				return "Sawamura Tomoki";
			}
			else if(CharID == JUN){
				if(bJapanese)
					return "井上純(いのうえ　じゅん)";
				return "Inoue Jun";
			}
			else if(CharID == MIHOKO){
				if(bJapanese)
					return "福路美穂子(ふくじ　みほこ)";
				return "Fukuji Mihoko";
			}
			else if(CharID == MIHARU){
				if(bJapanese)
					return "吉留未春(よしとめ　みはる)";
				return "Yoshitome Miharu";
			}
			else if(CharID == SEIKA){
				if(bJapanese)
					return "文堂星夏(ぶんどう　せいか)";
				return "Bundou Seika";
			}
			else if(CharID == SUMIYO){
				if(bJapanese)
					return "深堀純代(ふかぼり　すみよ)";
				return "Fukabori Sumiyo";
			}
			else if(CharID == YUMI){
				if(bJapanese)
					return "加治木ゆみ(かじき　ゆみ)";
				return "Kajiki Yumi";
			}
			else if(CharID == SATOMI){
				if(bJapanese)
					return "蒲原智美(かんばら　さとみ)";
				return "Kanbara Satomi";
			}
			else if(CharID == KAORI){
				if(bJapanese)
					return "妹尾佳織(せのう　かおり)";
				return "Senou Kaori";
			}
			else if(CharID == MUTSUKI){
				if(bJapanese)
					return "津山睦月(つやま　むつき)";
				return "Tsuyama Mutsuki";
			}
			
			return "";
		}
		
		public static String getSchool(int CharID, boolean bJapanese){
			if((CharID == SAKI)||(CharID == NODOKA)||(CharID == YUUKI)||(CharID == MAKO)||(CharID == HISA)){
				if(bJapanese)
					return "清澄高校";
				return "Kiyosumi High School";
			}
			if((CharID == KANA)||(CharID == MIHOKO)||(CharID == MIHARU)||(CharID == SEIKA)||(CharID == SUMIYO)){
				if(bJapanese)
					return "風越女子高校";
				return "Kazekoshi Girls' School";
			}
			if((CharID == KOROMO)||(CharID == TOUKA)||(CharID == HAJIME)||(CharID == TOMOKI)||(CharID == JUN)){
				if(bJapanese)
					return "龍門渕高校";
				return "Ryuumonbuchi High School";
			}
			if((CharID == MOMOKA)||(CharID == YUMI)||(CharID == SATOMI)||(CharID == KAORI)||(CharID == MUTSUKI)){
				if(bJapanese)
					return "鶴賀学園";
				return "Tsuruga Academy";
			}
			return "";
		}
		
		/**
		 * Return must be 6 lines!....er 4 lines
		 */
		public static String[] getBio(int CharID, boolean bJapanese){
			if(CharID == SAKI){
				if(bJapanese)
					return new String[] {"・高校１年生, 大将",
										"・10月27日生まれ",
										"・和が好きです",
										"・お姉さんはプロです"
										};
				return new String[] {"- First year student, team anchor",
									 "- Great at playing in person, but cannot play online",
									 "- Loves Nodoka",
									 "- Her estranged sister is a top-ranked mahjong player"
									 };
			}
			else if(CharID == NODOKA){
				if(bJapanese)
					return new String[] {"・高校１年生",
										"・10月4日生まれ",
										"・咲が好きです",
										"・全国中学生麻雀大会個人戦優勝の経験"
										};
				return new String[] {"- First year student",
									 "- National Middle School Individual Champion",
									 "- Always carries her penguin doll, Etopen",
									 "- Her online handle is 'Nodocchi'"
									 };
			}
			else if(CharID == YUUKI){
				if(bJapanese)
					return new String[] {"・高校１年生",
										"・9月16日生まれ",
										"・東場（ゲーム前半）では強い",
										""
										};
				return new String[] {"- First year student",
									 "- Strong in the east round, but weak in south",
									 "- Derives her mahjong powers from eating tacos",
									 "- Hates math"
									 };
			}
			else if(CharID == MAKO){
				if(bJapanese)
					return new String[] {"・高校2年生",
										"・5月5日生まれ",
										"・麻雀部次期主将",
										"・家は屋号の雀荘である"
										};
				return new String[] {"- Second year student",
									 "- Next in line to be captain",
									 "- Grew up in a mahjong parlor",
									 "- Removes her glasses when she plays"
									 };
			}
			else if(CharID == HISA){
				if(bJapanese)
					return new String[] {"・高校3年生",
										"・11月13日生まれ",
										"・学生議会長で麻雀部部長",
										""
										};
				return new String[] {"- Third year student",
									 "- Student Congress President",
									 "- Mahjong Club President",
									 "- Likes to use hell waits"
									 };
			}
			else if(CharID == KANA){
				if(bJapanese)
					return new String[] {"・高校２年生, 大将",
										"・2月22日生まれ",
										"・カプタン美穂子が好きです",
										"・ ～にゃあ"
										};
				return new String[] {"- Second year student, team anchor",
									 "- Loves Captain Mihoko",
									 "- Nya =3",
									 ""
									 };
			}
			else if(CharID == MIHOKO){
				if(bJapanese)
					return new String[] {"・高校3年生",
										"・9月24日生まれ",
										"・風越女子のキャプテンで",
										"・左目の色はブラウン、右目はブルー"
										};
				return new String[] {"- Third year student",
									 "- Mahjong Club Captain",
									 "- Eyes are two different colors",
									 ""
									 };
			}
			else if(CharID == MIHARU){
				if(bJapanese)
					return new String[] {"・高校２年生",
										"",
										"",
										""
										};
				return new String[] {"- Second Year Student",
									 "",
									 "",
									 ""
									 };
			}
			else if(CharID == SEIKA){
				if(bJapanese)
					return new String[] {"・高校１年生",
										"・校内ランキングは5位",
										"・学生議会長で麻雀部部長",
										""
										};
				return new String[] {"- First year student",
									 "- 5th ranked player in the school",
									 "- Student Congress member",
									 ""
									 };
			}
			else if(CharID == SUMIYO){
				if(bJapanese)
					return new String[] {"・高校２年生",
										"・口数が非常に少ない",
										"",
										""
										};
				return new String[] {"- Second year student",
									 "- Quiet",
									 "",
									 ""
									 };
			}
			else if(CharID == TOUKA){
				if(bJapanese)
					return new String[] {"・高校2年生",
										"・9月10日生まれ",
										"・麻雀部部長",
										"・祖父は龍門渕高校の理事長"
										};
				return new String[] {"- Second year student",
									 "- Granddaughter of the school preisdent",
									 "- Mahjong Club President",
									 "- ~desu wa"
									 };
			}
			else if(CharID == KOROMO){
				if(bJapanese)
					return new String[] {"・高校２年生, 大将",
										"・捨て子、透華と住めています",
										"・誕生日は九月六日です",
										""
										};
				return new String[] {"- Second year student, team anchor",
									 "- Her play is enhanced under a full moon",
									 "- Orphan, lives with Touka",
									 "- Her birthday is September 6"
									 };
			}
			else if(CharID == HAJIME){
				if(bJapanese)
					return new String[] {"・高校2年生",
										"・9月21日生まれ",
										"・透華のメイドでもある",
										"・ボクっ娘"
										};
				return new String[] {"- Second year student",
									 "- Serves as Touka's maid",
									 "- Caught cheating in a past tournament",
									 ""
									 };
			}
			else if(CharID == TOMOKI){
				if(bJapanese)
					return new String[] {"・高校2年生",
										"・3月10日生まれ",
										"・口数が少ない",
										"・いつもノートパソコンを持ち歩いており"
										};
				return new String[] {"- Second year student",
									 "- Quiet",
									 "- Strictly a 'digital' type player",
									 ""
									 };
			}
			else if(CharID == JUN){
				if(bJapanese)
					return new String[] {"・高校2年生",
										"・9月14日生まれ",
										"・長身",
										"・オレ女"
										};
				return new String[] {"- Second year student",
									 "- Extremely tall",
									 "- Often mistaken for a boy",
									 ""
									 };
			}
			else if(CharID == MOMOKA){
				if(bJapanese)
					return new String[] {"・高校１年生",
										"・7月26日生まれ",
										"・ゆみが好きです",
										"・影が薄く"
										};
				return new String[] {"- First year student, #2 player on her team",
									 "- Has a very weak presence, ignored by everyone",
									 "- Loves Yumi (who was the first person to notice her)",
									 ""
									 };
			}
			else if(CharID == YUMI){
				if(bJapanese)
					return new String[] {"・高校3年生, 大将",
										"・12月21日生まれ",
										"",
										""
										};
				return new String[] {"- Third year student, team anchor",
									 "- Strongest player on the team, but not the captain",
									 "",
									 ""
									 };
			}
			else if(CharID == SATOMI){
				if(bJapanese)
					return new String[] {"・高校3年生",
										"・麻雀部部長",
										"",
										""
										};
				return new String[] {"- Third year student",
									 "- Club president",
									 "- Wa-ha-ha",
									 ""
									 };
			}
			else if(CharID == KAORI){
				if(bJapanese)
					return new String[] {"・高校2年生",
										"・麻雀は素人",
										"",
										""
										};
				return new String[] {"- Second year student",
									 "- Complete amateur at mahjong",
									 "- Incredible beginner's luck",
									 ""
									 };
			}
			else if(CharID == MUTSUKI){
				if(bJapanese)
					return new String[] {"・高校2年生",
										"・蒲原の引退に伴い部長職を引き継ぎ",
										"",
										""
										};
				return new String[] {"- Second year student",
									 "- Will become the club president next year",
									 "",
									 ""
									 };
			}
			return new String[] {"",
								"",
								"",
								"",
								};
		}
		
		public static String getPower(int CharID, boolean bJapanese){
			if(CharID == SAKI){
				if(bJapanese)
					return "嶺上開花";
				return "Rinshan Kaihou";
			}
			else if(CharID == KOROMO){
				if(bJapanese)
					return "海底撈月";
				return "Haitei Raoyue";
			}
			else if(CharID == MOMOKA){
				if(bJapanese)
					return "見えない";
				return "Invisibility";
			}
			else if(CharID == HISA){
				if(bJapanese)
					return "地獄待ち";
				return "Hell Wait";
			}
			else if(CharID == MIHOKO){
				if(bJapanese)
					return "すべてを見る";
				return "See All";
			}
			else if(CharID == MAKO){
				if(bJapanese)
					return "すべてを見る";
				return "See All";
			}
			else if(CharID == KAORI){
				if(bJapanese)
					return "素人のまぐれ";
				return "Beginner's Luck";
			}
			else if(CharID == KANA){
				if(bJapanese)
					return "猫の咆哮";
				return "Kitty Roar";
			}
			else if(CharID == YUUKI){
				if(bJapanese)
					return "タコの力";
				return "Taco Power";
			}
			else{
				if(bJapanese)
					return "無い";
				return "None";
			}
			//return "";
		}
	}
	
	public static class Graphics{
		public static int DEFAULT_HEIGHT = 295;
		public static int DEFAULT_WIDTH = 480;
		
		//Misc constants
		public static class MISC{
			public static int BUBBLE_LEFT = 0;
			public static int BUBBLE_RIGHT = 1;
			public static int LOGO = 2;
			public static int CENTER = 3;
			public static int HALF_LEFT_ARROW = 4;
			public static int HALF_RIGHT_ARROW = 5;
			public static int RED_OUTLINE = 6;
			public static int LIGHTNING = 7;
			public static int RIICHISTICK = 8;
			public static int COUNTERSTICK = 9;
			
			public static int COUNT = 10;
		}
		
		public static class TileMisc{
			public static int EAST_BLANK = 0;
			public static int SOUTH_BLANK = 1;
			public static int WEST_BLANK = 2;
			public static int NORTH_BLANK = 3;
			public static int EAST_INVERT_BLANK = 4;
			public static int SOUTH_INVERT_BLANK = 5;
			public static int WEST_INVERT_BLANK = 6;
			public static int NORTH_INVERT_BLANK = 7;
			public static int MINI_BLANK = 8;
			public static int SHELL_TOP = 9;
			public static int SHELL_RIGHT = 10;
			public static int SHELL_BOTTOM = 11;
			public static int SHELL_LEFT = 12;
			public static int MINI_SHELL_TOP = 13;
			public static int MINI_SHELL_RIGHT = 14;
			public static int MINI_SHELL_BOTTOM = 15;
			public static int MINI_SHELL_LEFT = 16;
			public static int SOUTH_SHELL_TOP = 17;
			public static int SOUTH_SHELL_RIGHT = 18;
			public static int SOUTH_SHELL_BOTTOM = 19;
			public static int SOUTH_SHELL_LEFT = 20;
			public static int WEST_SHELL_TOP = 21;
			public static int WEST_SHELL_RIGHT = 22;
			public static int WEST_SHELL_BOTTOM = 23;
			public static int WEST_SHELL_LEFT = 24;
			public static int NORTH_SHELL_TOP = 25;
			public static int NORTH_SHELL_RIGHT = 26;
			public static int NORTH_SHELL_BOTTOM = 27;
			public static int NORTH_SHELL_LEFT = 28;
			
			//Inverted
			public static int SHELL_INVERT_TOP = 29;
			public static int SHELL_INVERT_RIGHT = 30;
			public static int SHELL_INVERT_BOTTOM = 31;
			public static int SHELL_INVERT_LEFT = 32;
			public static int MINI_INVERT_SHELL_TOP = 33;
			public static int MINI_INVERT_SHELL_RIGHT = 34;
			public static int MINI_INVERT_SHELL_BOTTOM = 35;
			public static int MINI_INVERT_SHELL_LEFT = 36;
			public static int SOUTH_INVERT_SHELL_TOP = 37;
			public static int SOUTH_INVERT_SHELL_RIGHT = 38;
			public static int SOUTH_INVERT_SHELL_BOTTOM = 39;
			public static int SOUTH_INVERT_SHELL_LEFT = 40;
			public static int WEST_INVERT_SHELL_TOP = 41;
			public static int WEST_INVERT_SHELL_RIGHT = 42;
			public static int WEST_INVERT_SHELL_BOTTOM = 43;
			public static int WEST_INVERT_SHELL_LEFT = 44;
			public static int NORTH_INVERT_SHELL_TOP = 45;
			public static int NORTH_INVERT_SHELL_RIGHT = 46;
			public static int NORTH_INVERT_SHELL_BOTTOM = 47;
			public static int NORTH_INVERT_SHELL_LEFT = 48;
			
			//Others
			public static int MINI_INVERT_BLANK = 49;
			public static int BIG_SHELL_TOP = 50;
			public static int BIG_SHELL_RIGHT = 51;
			public static int BIG_SHELL_BOTTOM = 52;
			public static int BIG_SHELL_LEFT = 53;
			
			public static int COUNT = 54;
		}
	}
	
	public static class ChangeLog{
		public static String[] FullLog = new String[]{
		"v1.0.0 Release Candidate - 06/28/11",
		"New Features:",
		"- Implemented Chankan",
		"- Implemented Nagashi Mangan",
		"- Added FAQ section",
/*60*/	"- Added slide controls to the character select screen",
		"- Added 'Romanji For Yaku' option",
		"- Enabled 'Move to SD' functionality",
		"AI Changes:",
		"- Tweak the way the AI handles yakuhai, honitsu, chinitsu, and itsu",
		"- Improve decision making when choosing discards in tenpai",
		"- Improve decision making for self-kans",
		"- AI will bail out of certain yaku (itsu, sanshoku doukou, doujun, shousangen, iipeikou) after it becomes impossible to complete",
		"Other:",
		"- Stop users from putting themselves in chombo when they call riichi",
/*50*/	"- Complete overhual of how we sort hands (to hopefully clear up some of the rare exceptions/errors I've seen)",
		"- Fix issue with AI players calling Kan when in riichi and glitching the game",
		"- Fixed an issue where the game wouldn't close properly when quiting in the middle of a game",
		"- Fixed issue where calling the last discard would end the hand before you could discard",
		"- Fixed an issue where promoted kans wouldn't activate Saki's power correctly",
		"- Fixed issue with scoring where ryanpeikou would always be treated as 7 pairs",
		"",
		"v0.2.1 Beta - 06/15/11",
		"- Replace all Bitmap buttons with programmatically drawn ones",
		"- Add FAQ, Change Log, Special Powers, Rules, & Yaku List under About/Help",
/*40*/	"- Replaced all tile images",
		"- Added 'Simplified Tiles' option",
		"- Added 'Drag To Discard' option",
		"- Moved 'Japanese' to the settings menu",
		"- Fix a ton of alignment issues",
		"- Fixed issue where we would hang around in RAM forever",
		"- Added random and undo options to character select screen",
		"- Added new icon for the app",
		"- Fixed issue with tsumo payments not being divided correctly",
		"- Better handling the back button/exiting the app",
/*30*/	"- Draw red tiles on the score screen",
		"- Added powers for Yuuki and Kana",
		"- Improved text scaling in a bunch of areas",
		"",
		"v0.2.0 Beta - 06/01/11",
		"- All Characters are now in the game",
		"- 3 new super powers added",
		"- Result screen added",
		"- Stat tracking and stat screen added (requires sd card)",
		"- Redesigned character select screen",
/*20*/	"- Slide to discard added",
		"- Added East-Only option",
		"- A lightning bolt will show when someone's powers activate",
		"- Game will actually go back to the title screen after a game is over",
		"- Game will end if a score goes below 0",
		"- Actually draw the red 5's red",
		"- Abortive draw scenarios added",
		"",
		"v0.1.1 Alpha - 05/11/11",
		"- 4 Characters added and a character select screen",
/*10*/	"- Saki, Momoka, and Koromo have their respective super powers",
		"- Fixed an issue where a player in riichi would sometimes be unable to call ron",
		"- Fixed an issue where Chinitsu hands would sometimes cause the game to glitch",
		"- The AI will now open up their hand for sanshoku doujun and doukou ",
		"- English is now the default language ",
		"- Fixed the double app drawer icon issue",
		"- Characters will announce Noten/Tenpai after an exhaustive draw",
		"- When asked to call a tile it will show you what tile you are being asked about in the center",
		"",
/*1*/	"v0.1.0 Alpha - 04/25/11"
		};
		
		public static int COUNT = 65;
	}
	
	//Scoring Table
	public static int[][] eastScoreTable = new int[][] {{0, 1500, 2000, 2400, 2900, 3400, 3900, 4400, 4800},
														{2400, 2900, 3900, 4800, 5800, 6800, 7700, 8700, 9600},
														{4800, 5800, 7700, 9600, 11600, 12000, 12000, 12000, 12000},
														{9600, 11600, 12000, 12000, 12000, 12000, 12000, 12000, 12000}};
	public static int[][] otherScoreTable = new int[][] {{0, 1000, 1300, 1600, 2000, 2300, 2600, 3200, 3900},
														 {1600, 2000, 2600, 3200, 3900, 4500, 5200, 5800, 6400},
														 {3200, 3900, 5200, 6400, 7700, 8000, 8000, 8000, 8000},
														 {6400, 7700, 8000, 8000, 8000, 8000, 8000, 8000, 8000}};
	public static int[] eastLimitTable = new int[] {12000, 18000, 24000, 36000, 48000};
	public static int[] otherLimitTable = new int[] {8000, 12000, 16000, 24000, 32000};
	
	static public void myAssert(boolean yesOrNo){
		if(!yesOrNo){
			int nothing = 1+1;
			nothing = nothing - 1;
			nothing--;
		}
	}
	
	static public String yakuToString(int yaku, boolean japanese, boolean romanji){
		if(yaku == PINFU){
			if(japanese)
				return "平和";
			else if(romanji)
				return "Pinfu";
			return "No-points hand";
		}
		if(yaku == TANYAO){
			if(japanese)
				return "断ヤオ九";
			else if(romanji)
				return "Tanyao";
			return "All Simples";
		}
		if(yaku == IIPEIKOU){
			if(japanese)
				return "一盃口";
			else if(romanji)
				return "Iipeikou";
			return "Identical Sequences";
		}
		if(yaku == YAKUHAI){
			if(japanese)
				return "翻牌";
			else if(romanji)
				return "Yakuhai";
			return "Wind/Dragon Pon";
		}
		if(yaku == SANSHOKUDOUJUN){
			if(japanese)
				return "三色同順";
			else if(romanji)
				return "Sanshoku Doujun";
			return "Three Color Straight";
		}
		if(yaku == ITSU){
			if(japanese)
				return "一気通貫";
			else if(romanji)
				return "Itsu";
			return "Straight";
		}
		if(yaku == CHANTA){
			if(japanese)
				return "全帯么";
			else if(romanji)
				return "Chanta";
			return "Terminal/Honor in Every Set";
		}
		if(yaku == HONROUTOU){
			if(japanese)
				return "混老頭)";
			else if(romanji)
				return "Honroutou";
			return "All Terminals & Honors";
		}
		if(yaku == TOITOI){
			if(japanese)
				return "対々和";
			else if(romanji)
				return "Toitoi";
			return "All Pons";
		}
		if(yaku == SANANKOU){
			if(japanese)
				return "三暗刻";
			else if(romanji)
				return "San Ankou";
			return "Three Concealed Pons";
		}
		if(yaku == SANKANTSU){
			if(japanese)
				return "三槓子";
			else if(romanji)
				return "San Kantsu";
			return "Three Kans";
		}
		if(yaku == SANSHOKUDOUKOU){
			if(japanese)
				return "三色同刻";
			else if(romanji)
				return "Sanshoku Doukou";
			return "Three Color Pons";
		}
		if(yaku == CHIITOI){
			if(japanese)
				return "七対子";
			else if(romanji)
				return "Chiitoi";
			return "Seven Pairs";
		}
		if(yaku == SHOUSANGEN){
			if(japanese)
				return "小三元";
			else if(romanji)
				return "Shousangen";
			return "Three Little Dragons";
		}
		if(yaku == HONITSU){
			if(japanese)
				return "混一色";
			else if(romanji)
				return "Honitsu";
			return "Half Flush";
		}
		if(yaku == JUNCHANTAYAO){
			if(japanese)
				return "純全帯么";
			else if(romanji)
				return "Junchantayao";
			return "Terminal in Each Set";
		}
		if(yaku == RYANPEIKOU){
			if(japanese)
				return "二盃口";
			else if(romanji)
				return "Ryanpeikou";
			return "Double Identical Sequences";
		}
		if(yaku == CHINITSU){
			if(japanese)
				return "清一色";
			else if(romanji)
				return "Chinitsu";
			return "Flush";
		}
		if(yaku == MENZEN){
			if(japanese)
				return "門前清自模和";
			else if(romanji)
				return "Menzen Tsumo";
			return "Self Draw";
		}
		if(yaku == RIICHI){
			if(japanese)
				return "立直";
			else if(romanji)
				return "Riichi";
			return "Ready Hand";
		}
		if(yaku == IPPATSU){
			if(japanese)
				return "一発";
			else if(romanji)
				return "Ippatsu";
			return "One Shot";
		}
		if(yaku == DOUBLERIICHI){
			if(japanese)
				return "ダブルリーチ";
			else if(romanji)
				return "Double Riichi";
			return "Double Ready Hand";
		}
		if(yaku == HAITEI){
			if(japanese)
				return "海底撈月";
			else if(romanji)
				return "Haitei Raoyue";
			return "Last Tile From Wall";
		}
		if(yaku == HOUTEI){
			if(japanese)
				return "河底撈魚";
			else if(romanji)
				return "Houtei Raoyui";
			return "Last Discard";
		}
		if(yaku == RINSHAN){
			if(japanese)
				return "嶺上開花";
			else if(romanji)
				return "Rinshan Kaihou";
			return "Tsumo After Kan";
		}
		if(yaku == CHANKAN){
			if(japanese)
				return "搶槓";
			else if(romanji)
				return "Chankan";
			return "Robbing a Kan";
		}
		if(yaku == NAGASHIMANGAN){
			if(japanese)
				return "流し満貫";
			else if(romanji)
				return "Nagashi Mangan";
			return "Terminal/Honor Discards";
		}
		if(yaku == KOKUSHUMUSOU){
			if(japanese)
				return "国士無双";
			else if(romanji)
				return "Kokushi Musou";
			return "Thirteen Orphans";
		}
		if(yaku == DAISANGEN){
			if(japanese)
				return "大三元";
			else if(romanji)
				return "Daisangen";
			return "Three Big Dragons";
		}
		if(yaku == SHOUSUUSHII){
			if(japanese)
				return "小四喜";
			else if(romanji)
				return "Shousuushii";
			return "Four Little Winds";
		}
		if(yaku == DAISUUSHII){
			if(japanese)
				return "大四喜";
			else if(romanji)
				return "Daisuushii";
			return "Four Big Winds";
		}
		if(yaku == CHUURENPOUTOU){
			if(japanese)
				return "九蓮宝燈";
			else if(romanji)
				return "Chuuren Poutou";
			return "Nine Gates";
		}
		if(yaku == SUUANKOU){
			if(japanese)
				return "四暗刻";
			else if(romanji)
				return "Suu Ankou";
			return "Four Concealed Pons";
		}
		if(yaku == RYUUIISOU){
			if(japanese)
				return "緑一色";
			else if(romanji)
				return "Ryuuiisou";
			return "All Green";
		}
		if(yaku == SUUKANTSU){
			if(japanese)
				return "四槓子";
			else if(romanji)
				return "Suu Kantsu";
			return "Four Kans";
		}
		if(yaku == TSUUIISOU){
			if(japanese)
				return "字一色";
			else if(romanji)
				return "Tsuuiisou";
			return "All Honors";
		}
		if(yaku == CHINROUTOU){
			if(japanese)
				return "清老頭";
			else if(romanji)
				return "Chinroutou";
			return "All Terminals";
		}
		if(yaku == TENHOU){
			if(japanese)
				return "天和";
			else if(romanji)
				return "Tenhou";
			return "Blessing of Heaven";
		}
		if(yaku == CHIIHOU){
			if(japanese)
				return "地和";
			else if(romanji)
				return "Chiihou";
			return "Blessing of Earth";
		}
		return "";
	}
	
	static public boolean needsToBeConcealed(int yakuToCheck){
		if(yakuToCheck == YAKUHAI || yakuToCheck == SANSHOKUDOUJUN || yakuToCheck == SANSHOKUDOUKOU || yakuToCheck == ITSU ||
		   yakuToCheck == CHANTA || yakuToCheck == HONROUTOU || yakuToCheck == TOITOI || yakuToCheck == SANKANTSU || yakuToCheck == SHOUSANGEN ||
		   yakuToCheck == HONITSU || yakuToCheck == CHINITSU)
			return false;
		
		return true;
	}
	
	static public String windToString(int wind){
		if(wind == Winds.EAST){
			return "東";
		}
		if(wind == Winds.SOUTH){
			return "南";
		}
		if(wind == Winds.WEST){
			return "西";
		}
		if(wind == Winds.NORTH){
			return "北";
		}
		return "";
	}
	
	static public String cmdToString(int cmd, boolean japanese){
		if(cmd == CMD.PASS){
			if(japanese)
				return "パス";
			return "Pass";
		}
		if(cmd == CMD.TSUMO){
			if(japanese)
				return "ツモ";
			return "Tsumo";
		}
		if(cmd == CMD.RON){
			if(japanese)
				return "ロン";
			return "Ron";
		}
		if(cmd == CMD.PON){
			if(japanese)
				return "ポン";
			return "Pon";
		}
		if(cmd == CMD.KAN || cmd == CMD.SELFKAN){
			if(japanese)
				return "カン";
			return "Kan";
		}
		if(cmd == CMD.CHI){
			if(japanese)
				return "千一";
			return "Chi";
		}
		if(cmd == CMD.RIICHI){
			if(japanese)
				return "リ一チ";
			return "Riichi";
		}
		if(cmd == CMD.TENPAI){
			if(japanese)
				return "てんぱい";
			return "Tenpai";
		}
		if(cmd == CMD.NOTEN){
			if(japanese)
				return "のてん";
			return "Noten";
		}
		return "";
	}
	
	public static String LimitHandToString(int han, boolean bJapanese){
		if(han < 5)
			return "";
		if(han == 5){
			if(bJapanese)
				return "満貫";
			return "Mangan";
		}
		else if(han <= 7){
			if(bJapanese)
				return "跳満";
			return "Haneman";
		}
		else if(han <= 9){
			if(bJapanese)
				return "倍満";
			return "Baiman";
		}
		else if(han <= 12){
			if(bJapanese)
				return "三倍満";
			return "Sanbaiman";
		}
		else if(han >= 13){
			if(bJapanese)
				return "役満";
			return "Yakuman";
		}
		return "";
	}
}

