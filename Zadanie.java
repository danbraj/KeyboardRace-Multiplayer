class Zadanie {

    private String text;
    private String[] words;
    private int counter = 0;
    private int wordsCount = 0;
    private int progress = 0;
    public static boolean SUCCESS = false;

    public Zadanie(String content) {
        this.text = content;
        this.words = text.split(" ");
        this.wordsCount = words.length;
        this.counter = 0;
        this.progress = 0;
        Zadanie.SUCCESS = false;
    }

    public String getText() {
        return this.text;
    }

    public boolean ifEqualsGoNext(String word) {
        if (!this.SUCCESS) {
            if (word.equals(this.words[this.counter] + " ")) {
                if (this.counter == this.wordsCount - 1)
                    Zadanie.SUCCESS = true;
                //else 
                    this.counter++;
                
                this.progress = (int)Math.floor(
                    ((double)this.counter / this.wordsCount) * 100
                );

                return true;
            } else return false;
        } else return false;
    }

    public int getProgress() {
        return this.progress;
    }
}