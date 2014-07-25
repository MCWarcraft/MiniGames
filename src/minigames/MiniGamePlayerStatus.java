package minigames;

public enum MiniGamePlayerStatus
{
	IN_LOBBY(0), IN_GAME(1);
	
	private int value;
	
	private MiniGamePlayerStatus(int value)
	{
		this.value = value;
	}
	
	public int getValue()
	{
		return value;
	}
}
