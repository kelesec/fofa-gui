package club.keleya.data;

public class Data {
    private int id;
    private String url;
    private String host;
    private int port;
    private String title;
    private String server;
    private Boolean isAlive;

    public Data() {
    }

    public Data(int id, String url, String host, int port, String title, String server, Boolean isAlive) {
        this.id = id;
        this.url = url;
        this.host = host;
        this.port = port;
        this.title = title;
        this.server = server;
        this.isAlive = isAlive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Boolean getIsAlive() {
        return isAlive;
    }

    public void setIsAlive(Boolean alive) {
        isAlive = alive;
    }

    @Override
    public String toString() {
        return "Data{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", title='" + title + '\'' +
                ", server='" + server + '\'' +
                ", isAlive=" + isAlive +
                '}';
    }
}
