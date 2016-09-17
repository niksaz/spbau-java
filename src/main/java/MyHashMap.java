/**
 * Created by niksaz on 17/09/16.
 */
public class MyHashMap {

    // хеш-таблица, использующая список

    // ключами и значениями выступают строки

    // стандартный способ получить хеш объекта -- вызвать у него метод hashCode()

    // сейчас все методы бросают исключение
    // это сделано, чтобы код компилировался, в конечном коде такого исключения быть не должно

    public int size() {
        // кол-во ключей в таблице
        throw new UnsupportedOperationException();
    }

    public boolean contains(String key) {
        // true, если такой ключ содержится в таблице
        throw new UnsupportedOperationException();
    }

    public String get(String key) {
        // возвращает значение, хранимое по ключу key
        // если такого нет, возвращает null
        throw new UnsupportedOperationException();
    }

    public String put(String key, String value) {
        // положить по ключу key значение value
        // и вернуть ранее хранимое, либо null
        throw new UnsupportedOperationException();
    }

    public String remove(String key) {
        // забыть про пару key-value для переданного key
        // и вернуть забытое value, либо null, если такой пары не было
        throw new UnsupportedOperationException();
    }

    public void clear() {
        // забыть про все пары key-value
        throw new UnsupportedOperationException();
    }
}
