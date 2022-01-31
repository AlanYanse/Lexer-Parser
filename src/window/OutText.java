package window;

import java.awt.*;

/**
 * è¾“å‡ºåŸŸï¼Œå°†è¯�æ³•åˆ†æž�ç»“æžœæˆ–è€…è¯­æ³•åˆ†æž�ç»“æžœè¾“å‡ºåˆ°è¿™ä¸ªåŸŸä¸­
 */
public class OutText extends TextArea{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OutText(int rows,int columns) {
        setBackground(Color.white);
        setForeground(Color.black);
        setRows(rows);
        setColumns(columns);
        setFont(new Font("Courier",1,12));
    }
}
