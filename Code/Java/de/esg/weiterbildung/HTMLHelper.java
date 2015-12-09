package de.esg.weiterbildung;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;

import com.ibm.xsp.model.domino.wrapped.DominoDocument;
import com.ibm.xsp.model.domino.wrapped.DominoRichTextItem;

public class HTMLHelper {
	private boolean debugMode = true;
    private String fieldName="dBody";
    private DominoDocument document;

    public String getFieldName(){
        return this.fieldName;
    }

    public void setFieldName(final String fieldName){
        this.fieldName = fieldName;
    }   

    public boolean isDebugMode(){
        return this.debugMode;
    }

    public void setDebugMode(final boolean debugMode){
        this.debugMode = debugMode;
    }
    public DominoDocument getDocument(){
        return this.document;
    }

    public void setDocument(final DominoDocument document){
        this.document = document;
    }

    public String getBodyHTML()throws NotesException{
        String back ="";

        if(null != document){
            if(this.isDebugMode()){
                System.out.println("Started getBodyHTML()");
            }
            final DominoRichTextItem drti = document.getRichTextItem(fieldName);
            if(null != drti){
                try {
                    String html = drti.getHTML();
                    if(this.isDebugMode()){
                        System.out.println("Completed getBodyHTML() : " + html);
                    }
                    return html;
                } catch (Exception e) {
                    if(this.isDebugMode()){
                        System.out.println("Failed getBodyHTML() : " + e.getMessage());
                    }
                }
            }
        }
        return back;
    }
    /*
     * Wraps a lotus.domino.Document as a com.ibx.xsp.model.domino.wrapped.DominoDocument, including a RichText item
     *
     * @param doc document to be wrapped
     *
     * @param richTextItemName name of the rich text item containing standard RichText or MIME  contents that need to be wrapped
     */
    public String getRichTextAsHTML(Document doc, final String richTextItemName) throws NotesException {
    	DominoDocument ddoc = wrapDocument(doc, richTextItemName);
    	DominoRichTextItem drti = ddoc.getRichTextItem(richTextItemName);
    	 
    	String html = drti.getHTML();
    	System.out.println(html);
    	return html;
    }
    public static DominoDocument wrapDocument(final Document doc, final String richTextItemName) throws NotesException {
     
      DominoDocument wrappedDoc = null;
     
      Database db = doc.getParentDatabase();
     
      //disable MIME to RichText conversion
      db.getParent().setConvertMIME(false);
     
      //wrap the lotus.domino.Document as a lotus.domino.DominoDocument
      //see http://public.dhe.ibm.com/software/dw/lotus/Domino-Designer/JavaDocs/DesignerAPIs/com/ibm/xsp/model/domino/wrapped/DominoDocument.html
      wrappedDoc = DominoDocument.wrap(doc.getParentDatabase().getFilePath(), doc, null, null, false, null, null);
     
      //see http://public.dhe.ibm.com/software/dw/lotus/Domino-Designer/JavaDocs/DesignerAPIs/com/ibm/xsp/model/domino/wrapped/DominoRichTextItem.html
      DominoRichTextItem drti = null;
     
      Item itemRT = doc.getFirstItem(richTextItemName);
     
      if (null != itemRT) {
     
        if (itemRT.getType() == Item.RICHTEXT) {
     
          //create a DominoRichTextItem from the RichTextItem
          RichTextItem rt = (RichTextItem) itemRT;
          drti = new DominoRichTextItem(wrappedDoc, rt);
     
        } else if (itemRT.getType() == Item.MIME_PART) {
     
          //create a DominoRichTextItem from the Rich Text item that contains MIME
          MIMEEntity rtAsMime = doc.getMIMEEntity(richTextItemName);
          drti = new DominoRichTextItem(wrappedDoc, rtAsMime, richTextItemName);
     
        }
      }
     
      wrappedDoc.setRichTextItem(richTextItemName, drti);
     
      return wrappedDoc;
     
    }

}
