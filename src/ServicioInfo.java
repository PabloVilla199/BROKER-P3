import java.io.Serializable;
import java.util.List;

public class ServicioInfo implements Serializable {
    private String nombreServidor;
    private List<String> parametros;
    private String tipoRetorno;

    public ServicioInfo(String nombreServidor, List<String> parametros, String tipoRetorno) {
        this.nombreServidor = nombreServidor;
        this.parametros = parametros;
        this.tipoRetorno = tipoRetorno;
    }

    public String getNombreServidor() {
        return nombreServidor;
    }

    public void setNombreServidor(String nombreServidor) {
        this.nombreServidor = nombreServidor;
    }

    public List<String> getParametros() {
        return parametros;
    }

    public void setParametros(List<String> parametros) {
        this.parametros = parametros;
    }

    public String getTipoRetorno() {
        return tipoRetorno;
    }

    public void setTipoRetorno(String tipoRetorno) {
        this.tipoRetorno = tipoRetorno;
    }
}