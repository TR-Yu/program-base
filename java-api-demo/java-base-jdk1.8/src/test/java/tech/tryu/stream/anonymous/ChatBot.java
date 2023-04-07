package tech.tryu.stream.anonymous;

public class ChatBot implements Bot{
    @Override
    public String botReturn() {
        return "chat bot";
    }

    @Override
    public Boolean isBot() {
        return true;
    }
}
