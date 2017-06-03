import java.io.Serializable;

class Zadanie implements Serializable {

    public boolean isSuccess = false;
    private String text;
    private String[] words;
    private int counter = 0;
    private int wordsCount = 0;
    private int progress = 0;

    public Zadanie(String text) {
        this.text = text;
        this.words = text.split(" ");
        this.wordsCount = words.length;
        this.counter = 0;
        this.progress = 0;
        this.isSuccess = false;
    }

    public String getText() {
        return this.text;
    }

    public int getProgress() {
        return this.progress;
    }

    public boolean ifEqualsGoNext(String word) {
        if (!this.isSuccess) {
            if (word.equals(this.words[this.counter])) {
                if (this.counter == this.wordsCount - 1)
                    this.isSuccess = true;
                this.counter++;
                this.progress = (int) Math.floor(((double) this.counter / this.wordsCount) * 100);
                return true;
            } else
                return false;
        } else
            return false;
    }
}