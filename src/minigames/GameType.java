package minigames;

public enum GameType
{
	LMS("lms"), KOTH("koth");
	
	private String value;
	
	private GameType(String value)
	{
		this.value = value;
	}
	
	public String toString()
	{
		return value;
	}
}
