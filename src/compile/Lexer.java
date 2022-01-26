package compile;

import entity.MultiComment;
import entity.SingleComment;
import entity.TokenList;
import frame.OutText;
import frame.ReadText;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @description This is the concrete implementation of lexical analysis
 */
@SuppressWarnings("all")
public class Lexer extends MainComplier{

    //Lexical analysis interpretation rules, defining character arrays
    String[] keyWords = {"if", "else", "while", "read", "write", "int", "double","for"};  //array of keywords
    String[] operator = {"+", "-", "*", "/"};//operator array
    String[] roperator = {">", "<", "==", "<>"};//å…³ç³»è¿�ç®—ç¬¦æ•°ç»„
    String[] sepretor = {";", "{", "}", "(", ")", "."};//åˆ†éš”ç¬¦æ•°ç»„
    String RegexToId = "^[a-zA-Z]([a-zA-Z_0-9])*[a-zA-Z0-9]$||[a-zA-Z]";//æ ‡è¯†ç¬¦çš„æ­£åˆ™è¡¨è¾¾å¼�
    String RegexToNumber = "^^-?\\d+$";//æ•´æ•°çš„æ­£åˆ™è¡¨è¾¾å¼�
    String RegexToFloat = "^(-?\\d+)(\\.\\d+)?$";//æµ®ç‚¹æ•°çš„æ­£åˆ™è¡¨è¾¾å¼�
    String RegexToArray = "[a-zA-Z]+(\\[[0-9][1-9]*\\])+";//æ•°ç»„å�˜é‡�çš„æ­£åˆ™è¡¨è¾¾å¼�

    //å°†çˆ¶ç±»çš„readText, outTextç»§æ‰¿è¿‡æ�¥
    public Lexer(ReadText readText, OutText outText) throws HeadlessException {
        super(readText, outText);
    }

    //åˆ†æž�è¿‡ç¨‹ï¼Œæ­¤å¤„ä¸ºè¯­æ³•åˆ†æž�å’Œè¯�æ³•åˆ†æž�æ��ä¾›å·²ç»�ä¿®é¥°è¿‡çš„æº�ç¨‹åº�
    public List<TokenList> getTokens() {
        List<TokenList> tokenLists = new ArrayList<>();//ç”¨äºŽè®°å½•Tokençš„ä¿¡æ�¯
        String inputText = readText.getText();
        StringTokenizer totalStrt = new StringTokenizer(inputText, "\r\n");
        int row = 0;//è¡Œå�·
        //èŽ·å�–æ‰€æœ‰çš„è®°å�·ä»¥å�Šè®°å�·çš„ä¿¡æ�¯
        while (totalStrt.hasMoreTokens()) {
            List<String> Tokens = new ArrayList<>();//è¡Œè®°å�·
            StringTokenizer rowOfStrt = new StringTokenizer(totalStrt.nextToken(), " \n\r\t;(){}\"\'+-<>/=*", true);
            //æ‰€æœ‰å�¯èƒ½çš„ç•Œç¬¦ï¼Œåˆ�æ­¥å¾—åˆ°æ‰€æœ‰çš„Token,ä½†éœ€è¦�è¿›ä¸€æ­¥çš„å�ˆå¹¶
            while (rowOfStrt.hasMoreTokens()) {
                Tokens.add(rowOfStrt.nextToken());
            }
            TokenList tokenList = new TokenList(row, Tokens);
            tokenLists.add(tokenList);
            row++;
        }
        //å¯¹äºŽåˆ�æ­¥å¾—åˆ°çš„è®°å�·é›†å�ˆçš„è¿›ä¸€æ­¥åˆ¤æ–­ä¸Žæ•´å�ˆ,ç”¨äºŽåŒºåˆ«æ³¨é‡Šå’Œ*,/ï¼›ä»¥å�Š=ä¸Ž==,ä»¥å�Š<ä¸Ž<>
        for (int i = 0; i < tokenLists.size(); i++) {
            List<String> tokenList = tokenLists.get(i).getTokenList();//èŽ·å�–è¡Œè®°å�·ç»„
            for (int j = 0; j < tokenList.size() - 1; j++) {
                if (tokenList.get(j).equals("/") && tokenList.get(j + 1).equals("/")) {
                    //å�•è¡Œæ³¨é‡Šè®°å�·çš„è¯†åˆ«
                    tokenList.set(j, "//");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("/") && tokenList.get(j + 1).equals("*")) {
                    //å¤šè¡Œæ³¨é‡Šçš„è¯†åˆ«
                    tokenList.set(j, "/*");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("*") && tokenList.get(j + 1).equals("/")) {
                    //å¤šè¡Œæ³¨é‡Šçš„è¯†åˆ«
                    tokenList.set(j, "*/");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("=") && tokenList.get(j + 1).equals("=")) {
                    tokenList.set(j, "==");
                    tokenList.remove(j + 1);
                } else if (tokenList.get(j).equals("<") && tokenList.get(j + 1).equals(">")) {
                    tokenList.set(j, "<>");
                    tokenList.remove(j + 1);//åˆ¤æ–­ä¸�ç­‰äºŽç¬¦å�·
                }
            }
        }
        //ç¬¬äºŒæ¬¡å¯¹è®°å�·è¿›è¡Œåˆ¤æ–­æ•´å�ˆï¼Œä¸»è¦�ç”¨äºŽåŽ»é™¤å�„ç§�åˆ†éš”ç¬¦
        for (int i = 0; i < tokenLists.size(); i++) {
            List<String> tokenList = tokenLists.get(i).getTokenList();//èŽ·å�–è¡Œè®°å�·ç»„
            String Pattern = "\\s+|\t|\r\n";
            int j = 0;
            while(j<tokenList.size())
            {
                if(tokenList.get(j).matches(Pattern))
                {
                    tokenList.remove(j);
                }
                else
                {
                    j++;
                }
            }
        }
        //ç¬¬ä¸‰æ¬¡å¯¹è®°å�·è¿›è¡ŒåŽ»é™¤æ³¨é‡Šï¼Œå¾—åˆ°çœŸæ­£çš„å®Œæ•´çš„è®°å�·
        List<MultiComment> multiComments = new ArrayList<>();//å­˜æ”¾å¤šè¡Œæ³¨é‡Šçš„ä½�ç½®ä¿¡æ�¯
        List<SingleComment> singleComments = new ArrayList<>();//å­˜æ”¾å�•è¡Œæ³¨é‡Šçš„ä½�ç½®ä¿¡æ�¯
        for (int i = 0; i < tokenLists.size(); i++)//å¤šè¡Œæ³¨é‡Šçš„è®°å�·èŽ·å�–
        {
            List<String> TokenOfrow = tokenLists.get(i).getTokenList();
            int rowCount = tokenLists.get(i).getRow();//å¤šè¡Œæ³¨é‡Šè¡Œå�·
            for (int j = 0; j < TokenOfrow.size(); j++) {
                if (TokenOfrow.get(j).equals("//")) {
                    SingleComment singleComment = new SingleComment(rowCount, j);
                    singleComments.add(singleComment);//è®°å½•å�•è¡Œæ³¨é‡Šä½�ç½®
                }
                if (TokenOfrow.get(j).equals("/*")) {
                    MultiComment multiComment = new MultiComment(rowCount, j, "/*");//jä¸ºåˆ—å�·
                    multiComments.add(multiComment);
                } else if (TokenOfrow.get(j).equals("*/")) {
                    MultiComment multiComment = new MultiComment(rowCount, j, "*/");
                    multiComments.add(multiComment);
                }
            }
        }
        for (int i = 0; i < multiComments.size(); i = i + 2)//åŽ»é™¤å¤šè¡Œæ³¨é‡Šä¸­çš„æ•´è¡Œæ³¨é‡Š
        {
            if ((multiComments.size() % 2) == 0 && i <= multiComments.size() - 2)//åˆ¤æ–­æ³¨é‡Šæ˜¯å�¦æœªé—­å�ˆ
            {
                if (multiComments.get(i).getComment().equals("/*") && multiComments.get(i + 1).getComment().equals("*/")) {
                    for (int j = multiComments.get(i).getRow() + 1; j < multiComments.get(i + 1).getRow(); j++) {
                        tokenLists.remove(j);
                    }
                    List<String> StartLine = tokenLists.get(multiComments.get(i).getRow()).getTokenList();//æ³¨é‡Šè¡Œèµ·å§‹
                    List<String> EndLine = tokenLists.get(multiComments.get(i + 1).getRow()).getTokenList();//æ³¨é‡Šè¡Œç»“æ�Ÿ
                    for (int j = multiComments.get(i).getColumn(); j < StartLine.size(); )//å› ä¸ºéš�ç�€å…ƒç´ çš„åˆ é™¤å‡�å°‘ï¼Œsizeå¤§å°�ä¹Ÿä¼šå�‘ç”Ÿæ”¹å�˜
                    {
                        StartLine.remove(j);
                    }
                    int position = multiComments.get(i).getColumn();//ä½�ç½®æŒ‡é’ˆ
                    for (int j = 0; j <= position; )//å�Œç�†ï¼Œå…ƒç´ çš„æ•°é‡�çš„å‡�å°‘å¯¼è‡´sizeæ”¹å�˜
                    {
                        EndLine.remove(j);
                        position--;
                    }
                }
            } else {
                outText.append("æ— æ³•ç»§ç»­åˆ†æž�");
                outText.append("ç¬¬" + multiComments.get(i).getRow() + "è¡Œç¬¬" + multiComments.get(i).getColumn() + "å¤„çš„æ³¨é‡Šæœªé—­å�ˆ");
                break;
            }
        }
        for (int i = 0; i < singleComments.size(); i++) {
            List<String> SignleLine = tokenLists.get(singleComments.get(i).getRow()).getTokenList();
            for (int j = singleComments.get(i).getColumn(); j < SignleLine.size(); ) {
                SignleLine.remove(j);//åŽ»é™¤å�•è¡Œæ³¨é‡Š
            }
        }
        return tokenLists;
    }

    //æ‰€æœ‰çš„è®°å�·å¤„ç�†éƒ½å�šå¥½ï¼Œæ­¤å¤„çº¯åˆ†æž�è®°å�·
    public void Analysis() {
        List<TokenList> tokenLists = getTokens();
        for (int i = 0; i < tokenLists.size(); i++) {
            List<String> tokenList = tokenLists.get(i).getTokenList();
            outText.append("--------------------------------------------------åˆ†æž�ç¬¬" + (i + 1) + "è¡Œ--------------------------------------------------" + "\r\n");
            for (int j = 0; j < tokenList.size(); j++) {
                int Count = 0;
                for (int k = 0; k < keyWords.length; k++) {
                    if (tokenList.get(j).equals(keyWords[k])) {
                        outText.append(tokenList.get(j) + " æ˜¯å…³é”®å­—" + "\r\n");
                        Count++;
                    }
                }
                for (int k = 0; k < operator.length; k++) {
                    if (tokenList.get(j).equals(operator[k])) {
                        outText.append(tokenList.get(j) + " æ˜¯è¿�ç®—ç¬¦" + "\r\n");
                        Count++;
                    }
                }
                for (int k = 0; k < roperator.length; k++) {
                    if (tokenList.get(j).equals(roperator[k])) {
                        outText.append(tokenList.get(j) + " æ˜¯å…³ç³»è¿�ç®—ç¬¦" + "\r\n");
                        Count++;
                    }
                }
                for (int k = 0; k < sepretor.length; k++) {
                    if (tokenList.get(j).equals(sepretor[k])) {
                        outText.append(tokenList.get(j) + " æ˜¯åˆ†éš”ç¬¦" + "\r\n");
                        Count++;
                    }
                }
                if (tokenList.get(j).matches(RegexToId) && (Count == 0)) {
                    outText.append(tokenList.get(j) + " æ˜¯æ ‡è¯†ç¬¦" + "\r\n");
                } else if (tokenList.get(j).matches(RegexToNumber)) {
                    outText.append(tokenList.get(j) + " æ˜¯æ•´æ•°" + "\r\n");
                } else if (tokenList.get(j).matches(RegexToFloat)) {
                    outText.append(tokenList.get(j) + " æ˜¯æµ®ç‚¹æ•°" + "\r\n");
                } else if (tokenList.get(j).matches(RegexToArray)) {
                    outText.append(tokenList.get(j) + " æ˜¯æ•°ç»„å�˜é‡�" + "\r\n");
                } else if (tokenList.get(j).equals("=")) {
                    outText.append(tokenList.get(j) + " æ˜¯ç­‰äºŽå�·" + "\r\n");
                } else if (Count == 0) {
                    outText.append(tokenList.get(j) + " æ ‡è¯†ç¬¦å‘½å��é”™è¯¯" + "\r\n");
                }
            }
        }
    }
}
