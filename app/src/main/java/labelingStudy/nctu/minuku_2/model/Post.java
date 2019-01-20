package labelingStudy.nctu.minuku_2.model;

public class Post {

    public String packageName;
    public String title;
    public String content;
    public String time;
    public Boolean check;

    public Post(String packageName, String title, String content, String time, Boolean check) {

        this.packageName = packageName;
        this.title = title;
        this.content = content;
        this.time = time;
        this.check = check;
    }
}
