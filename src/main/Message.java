package main;
class Message {
    String text;
    double life; // e.g. frames or milliseconds

    Message(String text, double life) {
        this.text = text;
        this.life = life;
    }
}
