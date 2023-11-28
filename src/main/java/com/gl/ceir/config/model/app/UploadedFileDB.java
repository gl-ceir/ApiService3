package com.gl.ceir.config.model.app;
 
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
 
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;



@Entity
@Getter
@Setter
@NoArgsConstructor

@DynamicInsert
@Table(name = "uploaded_file_to_sync")

public class UploadedFileDB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    	@ColumnDefault("''")
    private String txnId;

    private String filePath;

    private String fileName;

    private Long serverId;

    public UploadedFileDB(String txnId, String filePath, String fileName, Long serverId) {
        this.txnId = txnId;
        this.filePath = filePath;
        this.fileName = fileName;
        this.serverId = serverId;
    }

    @Override
    public String toString() {
        return "UploadedFileDB{" + "txnId=" + txnId + ", filePath=" + filePath + ", fileName=" + fileName + ", serverId=" + serverId + '}';
    }

}
