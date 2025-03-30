/**
 *
 * @author Pablo y Marcos
 */
import java.io.Serializable;

public class Respuesta implements Serializable {
    private String mensaje;
    private int codigo;

    public Respuesta(String mensaje, int codigo) {
        this.mensaje = mensaje;
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }
}
