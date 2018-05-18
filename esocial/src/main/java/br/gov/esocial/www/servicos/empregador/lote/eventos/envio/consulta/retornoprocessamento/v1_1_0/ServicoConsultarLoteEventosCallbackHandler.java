/**
 * ServicoConsultarLoteEventosCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.7.7  Built on : Nov 20, 2017 (11:41:20 GMT)
 */
package br.gov.esocial.www.servicos.empregador.lote.eventos.envio.consulta.retornoprocessamento.v1_1_0;


/**
 *  ServicoConsultarLoteEventosCallbackHandler Callback class, Users can extend this class and implement
 *  their own receiveResult and receiveError methods.
 */
public abstract class ServicoConsultarLoteEventosCallbackHandler {
    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking
     * Web service call is finished and appropriate method of this CallBack is called.
     * @param clientData Object mechanism by which the user can pass in user data
     * that will be avilable at the time this callback is called.
     */
    public ServicoConsultarLoteEventosCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public ServicoConsultarLoteEventosCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */
    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for consultarLoteEventos method
     * override this method for handling normal response from consultarLoteEventos operation
     */
    public void receiveResultconsultarLoteEventos(
        br.gov.esocial.www.servicos.empregador.lote.eventos.envio.consulta.retornoprocessamento.v1_1_0.ServicoConsultarLoteEventosStub.ConsultarLoteEventosResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from consultarLoteEventos operation
     */
    public void receiveErrorconsultarLoteEventos(java.lang.Exception e) {
    }
}
