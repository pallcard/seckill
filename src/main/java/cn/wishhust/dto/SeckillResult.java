package cn.wishhust.dto;

// 封装json

/**
 * 一个VO类(View Object)，属于DTO层，用来封装json结果，方便页面取值；在这里，将其设计成泛型，就可以和灵活地往里边封装各种类型的对象。
 *
 * 这里的success属性不是指秒杀执行的结果，而是指页面是否发送请求成功，至于秒杀之后是否成功的这个结果则是封装到了data属性里。
 *
 * @param <T>
 */
public class SeckillResult<T> {
    private boolean success;

    private T data;

    private String error;


    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
