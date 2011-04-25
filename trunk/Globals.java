package mahjong.riichi;

public class Globals {
	//Tile Classification
	public static class Suits{
		public static int BAMBOO = 1;
		public static int PIN = 2;
		public static int MAN = 3;
		public static int SANGEN = 4;
		public static int KAZE = 5;
	}
	
	public static int SIMPLE = 1;
	public static int TERMINAL = 2;
	public static int HONOR = 3;
	
	//General
	public static class Winds{
		public static int EAST = 0;
		public static int SOUTH = 1;
		public static int WEST = 2;
		public static int NORTH = 3;
	}
	
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
	}
	
	//ID numbers
	public static int SCORESCREEN = 1;
	
	//Others
	public static String VERSION = "v0.1.0 Alpha";
	
	//Character IDs
	public static class Characters{
		public static int GENERIC = 0;
		public static int SAKI = 1;
		public static int KANA = 2;
		public static int KOROMO = 3;
		
		public static int COUNT = 4;
	}
	
	public static class Graphics{
		public static int DEFAULT_HEIGHT = 295;
		public static int DEFAULT_WIDTH = 480;
		
		//Misc constants
		public static int MISC_BUBBLE_LEFT = 0;
		public static int MISC_BUBBLE_RIGHT = 1;
		public static int MISC_LOGO = 2;
		public static int MISC_START_BTN = 3;
		public static int MISC_OK_BTN = 4;
		public static int MISC_CENTER = 5;
		public static int MISC_COUNT = 6;
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
	
	static public String yakuToString(int yaku, boolean japanese){
		if(yaku == PINFU){
			if(japanese)
				return "平和";
			return "Pinfu";
		}
		if(yaku == TANYAO){
			if(japanese)
				return "断ヤオ九";
			return "Tanyao";
		}
		if(yaku == IIPEIKOU){
			if(japanese)
				return "一盃口";
			return "Iipeikou";
		}
		if(yaku == YAKUHAI){
			if(japanese)
				return "翻牌";
			return "Yakuhai";
		}
		if(yaku == SANSHOKUDOUJUN){
			if(japanese)
				return "三色同順";
			return "Sanshoku Doujun";
		}
		if(yaku == ITSU){
			if(japanese)
				return "一気通貫";
			return "Itsu";
		}
		if(yaku == CHANTA){
			if(japanese)
				return "全帯么";
			return "Chanta";
		}
		if(yaku == HONROUTOU){
			if(japanese)
				return "混老頭)";
			return "Honroutou";
		}
		if(yaku == TOITOI){
			if(japanese)
				return "対々和";
			return "Toitoi";
		}
		if(yaku == SANANKOU){
			if(japanese)
				return "三暗刻";
			return "San Ankou";
		}
		if(yaku == SANKANTSU){
			if(japanese)
				return "三槓子";
			return "Sak Kantsu";
		}
		if(yaku == SANSHOKUDOUKOU){
			if(japanese)
				return "三色同刻";
			return "Sanshoku Doukou";
		}
		if(yaku == CHIITOI){
			if(japanese)
				return "七対子";
			return "Chiitoi";
		}
		if(yaku == SHOUSANGEN){
			if(japanese)
				return "小三元";
			return "Shousangen";
		}
		if(yaku == HONITSU){
			if(japanese)
				return "混一色";
			return "Honitsu";
		}
		if(yaku == JUNCHANTAYAO){
			if(japanese)
				return "純全帯么";
			return "Junchantayao";
		}
		if(yaku == RYANPEIKOU){
			if(japanese)
				return "二盃口";
			return "Ryanpeikou";
		}
		if(yaku == CHINITSU){
			if(japanese)
				return "清一色";
			return "Chinitsu";
		}
		if(yaku == MENZEN){
			if(japanese)
				return "門前清自模和";
			return "Menzen Tsumo";
		}
		if(yaku == RIICHI){
			if(japanese)
				return "立直";
			return "Riichi";
		}
		if(yaku == IPPATSU){
			if(japanese)
				return "一発";
			return "Ippatsu";
		}
		if(yaku == DOUBLERIICHI){
			if(japanese)
				return "ダブルリーチ";
			return "Double Riichi";
		}
		if(yaku == HAITEI){
			if(japanese)
				return "海底撈月";
			return "Haitei Raoyue";
		}
		if(yaku == HOUTEI){
			if(japanese)
				return "河底撈魚";
			return "Houtei Raoyui";
		}
		if(yaku == RINSHAN){
			if(japanese)
				return "嶺上開花";
			return "Rinshan Kaihou";
		}
		if(yaku == CHANKAN){
			if(japanese)
				return "搶槓";
			return "Chankan";
		}
		if(yaku == NAGASHIMANGAN){
			if(japanese)
				return "流し満貫";
			return "Nagashi Mangan";
		}
		if(yaku == KOKUSHUMUSOU){
			if(japanese)
				return "国士無双";
			return "Thirteen Orphans";
		}
		if(yaku == DAISANGEN){
			if(japanese)
				return "大三元";
			return "Three Big Dragons";
		}
		if(yaku == SHOUSUUSHII){
			if(japanese)
				return "小四喜";
			return "Four Little Winds";
		}
		if(yaku == DAISUUSHII){
			if(japanese)
				return "大四喜";
			return "Four Big Winds";
		}
		if(yaku == CHUURENPOUTOU){
			if(japanese)
				return "九蓮宝燈";
			return "Nine Gates";
		}
		if(yaku == SUUANKOU){
			if(japanese)
				return "四暗刻";
			return "Four Concealed Pons";
		}
		if(yaku == RYUUIISOU){
			if(japanese)
				return "緑一色";
			return "All Green";
		}
		if(yaku == SUUKANTSU){
			if(japanese)
				return "四槓子";
			return "Four Kans";
		}
		if(yaku == TSUUIISOU){
			if(japanese)
				return "字一色";
			return "All Honors";
		}
		if(yaku == CHINROUTOU){
			if(japanese)
				return "清老頭";
			return "All Terminals";
		}
		if(yaku == TENHOU){
			if(japanese)
				return "天和";
			return "Blessing of Heaven";
		}
		if(yaku == CHIIHOU){
			if(japanese)
				return "地和";
			return "Blessing Of Earth";
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
		return "";
	}
}
