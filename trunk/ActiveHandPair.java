package mahjong.riichi;

public class ActiveHandPair extends Object implements Comparable<ActiveHandPair> {
	int rawNumber;
	int rawHandIdx;

	public ActiveHandPair() {
		rawNumber = 0;
		rawHandIdx = 0;
	}
	
	public ActiveHandPair(int rawNumberToUse, int indexToUse){
		rawNumber = rawNumberToUse;
		rawHandIdx = indexToUse;
	}

	@Override
	public int compareTo(ActiveHandPair compareTo) {
		if(rawNumber < compareTo.rawNumber)
			return -1;
		else if(rawNumber > compareTo.rawNumber)
			return 1;
		return 0;
	}
	
	public boolean equals(Object obj){
		if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        ActiveHandPair thatOne = (ActiveHandPair) obj;
		if(/*rawNumber == thatOne.rawNumber && */rawHandIdx == thatOne.rawHandIdx)
			return true;
		return false;
	}

}
