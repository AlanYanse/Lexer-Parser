package window;

import java.awt.*;

/**
 * è¾“å…¥åŒºåŸŸï¼Œè¯»å�–æ–‡æœ¬åŸŸçš„ä¿¡æ�¯
 */
public class ReadText extends TextArea{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReadText(int rows,int columns) {
        setBackground(Color.white);
        setForeground(Color.black);
        setRows(rows);
        setColumns(columns);
    }
}
