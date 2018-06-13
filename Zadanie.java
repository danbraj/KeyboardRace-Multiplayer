import java.io.Serializable;

public class Zadanie implements Serializable {

    private boolean isFinished = false;
    private int counter = 0;
    private int progress = 0;
    private int wordsCount = 0;
    private String text;
    private String[] words;

    public Zadanie(String text) {
        this.isFinished = false;
        this.counter = 0;
        this.progress = 0;
        this.text = text;
        this.words = text.split(" ");
        this.wordsCount = words.length;
    }

    public String getText() {
        return this.text;
    }

    public int getProgress() {
        return this.progress;
    }

    public boolean wordPadIfEquals(String word) {
        if (!this.isFinished) {
            if (word.equals(this.words[this.counter])) {
                if (this.counter == this.wordsCount - 1)
                    this.isFinished = true;
                this.counter++;
                this.progress = Common.percentage(this.counter, this.wordsCount);
                return true;
            } else
                return false;
        } else
            return false;
    }

    public boolean isFinished() {
        return this.isFinished;
    }
}