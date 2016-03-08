package test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(TextEditor.class, SpellChecker.class);
        ctx.refresh();

        TextEditor te = (TextEditor) ctx.getBean("textEditor");

        te.spellCheck();
    }
}