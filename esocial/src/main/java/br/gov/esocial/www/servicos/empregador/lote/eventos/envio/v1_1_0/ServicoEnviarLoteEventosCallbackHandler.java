/**
 * ServicoEnviarLoteEventosCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.7.7  Built on : Nov 20, 2017 (11:41:20 GMT)
 */
package br.gov.esocial.www.servicos.empregador.lote.eventos.envio.v1_1_0;


/**
 *  ServicoEnviarLoteEventosCallbackHandler Callback class, Users can extend this class and implement
 *  their own receiveResult and receiveError methods.
 */
public abstract class ServicoEnviarLoteEventosCallbackHandler {
    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking
     * Web service call is finished and appropriate method of this CallBack is called.
     * @param clientData Object mechanism by which the user can pass in user data
     * that will be avilable at the time this callback is called.
     */
    public ServicoEnviarLoteEventosCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public ServicoEnviarLoteEventosCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */
    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for enviarLoteEventos method
     * override this method for handling normal response from enviarLoteEventos operation
     */
    public void receiveResultenviarLoteEventos(
        br.gov.esocial.www.servicos.empregador.lote.eventos.envio.v1_1_0.ServicoEnviarLoteEventosStub.EnviarLoteEventosResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from enviarLoteEventos operation
     */
    public void receiveErrorenviarLoteEventos(java.lang.Exception e) {
    }
}
