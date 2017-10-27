package codebits.tjah;

/**
 * Created by tjah on 18/10/17.
 */
public class Currency {
    private String name;
    private String symbol;
    private int thumbnail;

    public Currency(String name, String symbol, int thumbnail) {
        this.name = name;
        this.symbol = symbol;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }
}
