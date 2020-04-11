package driver


import java.util.logging.Logger;
import groovy.sql.Sql
import java.sql.SQLException
import etherip.*
import etherip.types.CIPData

/**
 * @author Ibere Matias 2018
 */
class PCEthIP {
    private int sampleTime = 1
    private def arrayMaquinas = null
    public def dbHandle = null
    public EtherNetIP plc = null
    private static final log = Logger.getLogger('PCEthIP')
    public def ds = null
    
    static void main(String[] args) {
        def pcethIP = new PCEthIP()
    }
    
    private void waitConnection(dbHandle) {
        def refused = true
        while (refused) {
            try{
                def test = dbHandle?.getDataSource()?.getConnection()
                refused = !test
            } catch (SQLException ex) {
                log.severe("waitConnection - "+ex.printStackTrace())
                sleep(5000)
            }
        }
    }
    
    def getTagsCLP() {
        def result = []
        try {
            def ds = getDs(dbHandle)
            if (arrayMaquinas) return arrayMaquinas
            ds.eachRow('SELECT id, ativo, nome, descricao, tipo FROM tag WHERE ativo = true ORDER BY id') {
                result << new TagCLP(id:it.id, nome:it.nome, descricao:it.descricao, tipo:it.tipo)
            }
            ds.close()
        } catch (Exception ex) {
            println ex.getMessage()
        }
        return result
    }
    
    def getDs(dbHandle) {
        if (dbHandle) {
            waitConnection(dbHandle)
        } else {
            def source = new org.postgresql.ds.PGPoolingDataSource()
            source.databaseName = 'my_database' // NAME DATABASE
            source.setServerName("localhost") // NAME OR ADD IP SERVER
            source.user = 'user'
            source.password = 'password'
            dbHandle = new Sql(source)
            waitConnection(dbHandle)
            log.info('  Connected DB..\n')
        }
        return dbHandle
    }
    
    private PCEthIP() {
            this.run()
    }
        void run() {
            Thread.start() {
                try {
                    sampleTime = 60000
                    CIPData value
                    ds = getDs(dbHandle)
                    EtherNetIP plc = new EtherNetIP("192.168.24.220",0); // ADD IP PLC AND NUMBER SOCKET CPU
                    plc.connectTcp()
                    while(true){
                        try {
                            println "*** Connected  ***"
                            // READ TAGS DATA BASE
                            tagsCLP.each(){ tagclp ->
                                // READ TAGS FROM PLC
                                value = plc.readTag(tagclp.nome)
                                // INSERT TAGS ON DATA BASE
                                insertBD(ds, tagclp.id, value.getNumber(0))
                            }
                            Thread.sleep(sampleTime)
                        } catch (Exception ex) {
                            Thread.sleep(sampleTime)
                            println ex.getMessage()
                        } 
                    }
                    plc.close()
                    ds.close() 
                    Thread.sleep(sampleTime)
                } catch (Exception ex) {
                    ds.close() 
                    println ex.getMessage()
                }  
                Thread.sleep(sampleTime)
            }      
    }
    
    
    void insertBD(def ds, def id_tag, def vp) {
        try {
            def ssql = " INSERT INTO medicao_vp (id, version, datahora, vp, tag_real_id) VALUES ( nextval('medicao_vp_id_seq'), 0, CURRENT_TIMESTAMP, "+ vp.toString() +", "+ id_tag +" ) "
            def keys = ds.executeInsert(ssql)
        } catch (Exception ex){
            println ex.getMessage()
        }
    }
	
}

