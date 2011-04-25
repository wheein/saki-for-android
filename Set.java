package mahjong.riichi;

public class Set {
	int size;
	int[] tiles;

	Set(){
		size = 0;
		tiles = new int[] {0,0,0,0};
	}
	Set(int tile1, int tile2, int tile3){
		size = 3;
		tiles = new int[] {tile1,tile2,tile3,0};
	}
	Set(int tile1, int tile2, int tile3, int tile4){
		size = 4;
		tiles = new int[] {tile1,tile2,tile3,tile4};
	}
	Set(int tile1, int tile2){
		size = 2;
		tiles = new int[] {tile1,tile2,0,0};
	}
	Set(int tile1){
		size = 1;
		tiles = new int[] {tile1,0,0,0};
	}
		
	void set(int tile1, int tile2, int tile3){
		size = 3;
		tiles[0] = tile1;
		tiles[1] = tile2;
		tiles[2] = tile3;
	}
	void set(int tile1, int tile2){
		size = 2;
		tiles[0] = tile1;
		tiles[1] = tile2;
	}
	void set(int tile1){
		size = 1;
		tiles[0] = tile1;
	}
		
	int numberMissing(){
		if(size >= 3)
			return 0;
		else if(size == 2)
			return 1;
		else if(size == 1)
			return 2;
		else
			return 3;
	}
		
	public boolean isPair(){
		if((size == 2) && (tiles[0] == tiles[1]))
			return true;
		return false;
	}
	public boolean isComplete(){
		if(size >= 3)
			return true;
		return false;
	}
	public boolean isChi(){
		if(size < 3)
			return false;
		return (tiles[0] != tiles[1]);
	}
	
	public boolean equals(Set compareTo){
		if(size != compareTo.size)
			return false;
		for(int i = 0; i < size; i++){
			if(tiles[i] != compareTo.tiles[i])
				return false;
		}
		return true;
	}
}
