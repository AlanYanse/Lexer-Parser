package compile;

import frame.OutText;
import frame.ReadText;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.PublicKey;
import java.util.*;

/**
 * @description This is the concrete implementation of lexical analysis
 */
@SuppressWarnings("all")
public class Parser extends MainComplier{
    //rules for parsing
    public static final String PATH = "./grammar2";// grammar
    private static String START; // start symbol
    private static HashSet<String> VN, VT; // set of nonterminal symbols, set of terminal symbols
    private static HashMap<String, ArrayList<ArrayList<String>>> MAP;// key:The left side of the production value: the right side of the production (including multiple)
    private static HashMap<String, String> oneLeftFirst;// "|" The FIRST set corresponding to the separate single production is used to construct the prediction analysis table
    private static HashMap<String, HashSet<String>> FIRST, FOLLOW; // FIRST, FOLLOW collection
    private static String[][] FORM; // An array of predictive analysis tables for output
    private static HashMap<String, String> preMap;// The map that stores the predictive analysis table for fast lookup
    private int choice;

    //Inherit the readText and outText of the parent class
    public Parser(ReadText readText, OutText outText, int choice) throws HeadlessException {
        super(readText, outText);
        this.choice=choice;
    }

    //Program entry
    public void Main() {
        init(); // Initialize variables
        identifyVnVt(readFile(new File(PATH)));//Symbol classification, and stored in MAP in the form of key-value
        reformMap();// Eliminate left recursion and extract left common factor
        findFirst(); // Find the FIRST set
        findFollow(); // Find the FOLLOW set
        if (isLL1()) {
            preForm(); // Build a predictive analytics table
            printAutoPre(readText.getText());
        }
    }
    // Read grammar from file
    public ArrayList<String> readFile(File file) {
        BufferedReader br = null;
        outText.append("The grammar read from the file is: "+"\r\n"); // 
        ArrayList<String> result = new ArrayList<>();
        try {
            if (choice == 1) {
                br = new BufferedReader(new FileReader("F:/grammer//a1.txt"));
            } else if (choice == 2){
                br = new BufferedReader(new FileReader("F:/grammer//a2.txt"));
            } else if (choice == 3){
                br = new BufferedReader(new FileReader("F:/grammer//a3.txt"));
            } else if (choice == 4){
                br = new BufferedReader(new FileReader("F:/grammer//a4.txt"));
            }
            String s = null;
            while ((s = br.readLine()) != null) {
                outText.append("\t" + s+"\r\n");
                result.add(s.trim());
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    // variable initialization
    private static void init() {
        VN = new HashSet<>();
        VT = new HashSet<>();
        MAP = new HashMap<>();
        FIRST = new HashMap<>();
        FOLLOW = new HashMap<>();
        oneLeftFirst = new HashMap<>();
        preMap = new HashMap<>();
    }
    // Symbol classification
    private void identifyVnVt(ArrayList<String> list) {
        START = list.get(0).charAt(0) + "";// save start symbol

        for (int i = 0; i < list.size(); i++) {
            String oneline = list.get(i);
            String[] vnvt = oneline.split("â†’");// split by definition symbol
            String left = vnvt[0].trim(); // left side of grammar
            VN.add(left);

            // grammar right
            ArrayList<ArrayList<String>> mapValue = new ArrayList<>();
            ArrayList<String> right = new ArrayList<>();

            for (int j = 0; j < vnvt[1].length(); j++) { // Use "|" to split the right
                if (vnvt[1].charAt(j) == '|') {
                    VT.addAll(right);
                    mapValue.add(right);
                    // right.clear();// After clearing, it is still the same address, and you need to renew the object
                    right = null;
                    right = new ArrayList<>();
                    continue;
                }
                // If the left side of a character in the production contains a single quotation mark in Chinese or English, it is regarded as the same character
                if (j + 1 < vnvt[1].length() && (vnvt[1].charAt(j + 1) == '\'' || vnvt[1].charAt(j + 1) == '’')) {
                    right.add(vnvt[1].charAt(j) + "" + vnvt[1].charAt(j + 1));
                    j++;
                } else {
                    right.add(vnvt[1].charAt(j) + "");
                }
            }
            VT.addAll(right);
            mapValue.add(right);

            MAP.put(left, mapValue);
        }
        VT.removeAll(VN); // Remove nonterminals from the terminal character set
        // print Vn, Vt
        outText.append("\nVné›†å�ˆ:\r\n\t{" + String.join("ã€�", VN.toArray(new String[VN.size()])) + "}"+"\r\n");
        outText.append("Vté›†å�ˆ:\n\t{" + String.join("ã€�", VT.toArray(new String[VT.size()])) + "}"+"\r\n");

    }
    // Eliminate direct left recursion //TODO
    private void reformMap() {
        boolean isReForm = false;// Whether the MAP has been modified
        Set<String> keys = new HashSet<>();
        keys.addAll(MAP.keySet());
        Iterator<String> it = keys.iterator();
        ArrayList<String> nullSign = new ArrayList<>();
        nullSign.add("Îµ");
        while (it.hasNext()) {
            String left = it.next();
            boolean flag = false;// Is there left recursion
            ArrayList<ArrayList<String>> rightList = MAP.get(left);
            ArrayList<String> oldRightCell = new ArrayList<>(); // old generated right
            ArrayList<ArrayList<String>> newLeftNew = new ArrayList<>();// store new left and new right

            // Eliminate direct left recursion
            for (int i = 0; i < rightList.size(); i++) {
                ArrayList<String> newRightCell = new ArrayList<>(); // the right side of the new production
                if (rightList.get(i).get(0).equals(left)) {
                    for (int j = 1; j < rightList.get(i).size(); j++) {
                        newRightCell.add(rightList.get(i).get(j));
                    }
                    flag = true;
                    newRightCell.add(left + "\'");
                    newLeftNew.add(newRightCell);
                } else {
                    for (int j = 0; j < rightList.get(i).size(); j++) {
                        oldRightCell.add(rightList.get(i).get(j));
                    }
                    oldRightCell.add(left + "\'");
                }
            }
            // If there is left recursion, update MAP
            if (flag) {
                isReForm = true;
                newLeftNew.add(nullSign);
                MAP.put(left + "\'", newLeftNew);
                VN.add(left + "\'"); // åŠ å…¥æ–°çš„VN
                VT.add("Îµ"); // åŠ å…¥Îµåˆ°VT
                ArrayList<ArrayList<String>> newLeftOld = new ArrayList<>();// save the original, but generate a new right (...Continue from here...)
                newLeftOld.add(oldRightCell);
                MAP.put(left, newLeftOld);
            }
        }
        // If the grammar is modified, output the modified grammar
        if (isReForm) {
            outText.append("Eliminate left recursion of grammar:"+"\r\n");
            Set<String> kSet = new HashSet<>(MAP.keySet());
            Iterator<String> itk = kSet.iterator();
            while (itk.hasNext()) {
                String k = itk.next();
                ArrayList<ArrayList<String>> leftList = MAP.get(k);
                outText.append("\t" + k + "â†’");
                for (int i = 0; i < leftList.size(); i++) {
                    outText.append(String.join("", leftList.get(i).toArray(new String[leftList.get(i).size()])));
                    if (i + 1 < leftList.size()) {
                        outText.append("|");
                    }
                }
                outText.append("\r\n");
            }
        }
    }
    // find the FIRST set for each nonterminal and factorize the FIRST set of a single production
    private void findFirst() {
        outText.append("\nFIRSTé›†å�ˆ:"+"\r\n");
        Iterator<String> it = VN.iterator();
        while (it.hasNext()) {
            HashSet<String> firstCell = new HashSet<>();// FIRST holding a single nonterminal
            String key = it.next();
            ArrayList<ArrayList<String>> list = MAP.get(key);
            // System.out.println(key+":");
            // traverse the left side of a single production
            for (int i = 0; i < list.size(); i++) {
                ArrayList<String> listCell = list.get(i);// listCell is divided by "|"
                HashSet<String> firstCellOne = new HashSet<>();// First of a single expression separated by " | " on the left side of the production (deprecated)
                String oneLeft = String.join("", listCell.toArray(new String[listCell.size()]));
                // System.out.println("oneLeft: "+oneLeft);
                if (VT.contains(listCell.get(0))) {
                    firstCell.add(listCell.get(0));
                    firstCellOne.add(listCell.get(0));
                    oneLeftFirst.put(key + "$" + listCell.get(0), key + "â†’" + oneLeft);
                } else {
                    boolean[] isVn = new boolean[listCell.size()];// Whether the token is defined as empty, if so check for the next character
                    isVn[0] = true;// ç¬¬ä¸€ä¸ªä¸ºé�žç»ˆç»“ç¬¦å�·
                    int p = 0;
                    while (isVn[p]) {
                        // System.out.println(p+" "+listCell.size());
                        if (VT.contains(listCell.get(p))) {
                            firstCell.add(listCell.get(p));
                            firstCellOne.add(listCell.get(p));
                            oneLeftFirst.put(key + "$" + listCell.get(p), key + "â†’" + oneLeft);
                            break;
                        }
                        String vnGo = listCell.get(p);//
                        Stack<String> stack = new Stack<>();
                        stack.push(vnGo);
                        while (!stack.isEmpty()) {
                            ArrayList<ArrayList<String>> listGo = MAP.get(stack.pop());
                            for (int k = 0; k < listGo.size(); k++) {
                                ArrayList<String> listGoCell = listGo.get(k);
                                if (VT.contains(listGoCell.get(0))) { // å¦‚æžœç¬¬ä¸€ä¸ªå­—ç¬¦æ˜¯ç»ˆç»“ç¬¦å�·
                                    if ("Îµ".equals(listGoCell.get(0))) {
                                        if (!key.equals(START)) { // å¼€å§‹ç¬¦å�·ä¸�èƒ½æŽ¨å‡ºç©º
                                            firstCell.add(listGoCell.get(0));
                                            firstCellOne.add(listGoCell.get(0));
                                            oneLeftFirst.put(key + "$" + listGoCell.get(0), key + "â†’" + oneLeft);
                                        }
                                        if (p + 1 < isVn.length) {// å¦‚æžœä¸ºç©ºï¼Œå�¯ä»¥æŸ¥è¯¢ä¸‹ä¸€ä¸ªå­—ç¬¦
                                            isVn[p + 1] = true;
                                        }
                                    } else { // é�žç©ºçš„ç»ˆç»“ç¬¦å�·åŠ å…¥å¯¹åº”çš„FIRSTé›†å�ˆ
                                        firstCell.add(listGoCell.get(0));
                                        firstCellOne.add(listGoCell.get(0));
                                        oneLeftFirst.put(key + "$" + listGoCell.get(0), key + "â†’" + oneLeft);
                                    }
                                } else {// ä¸�æ˜¯ç»ˆç»“ç¬¦å�·ï¼Œå…¥æ ˆ
                                    stack.push(listGoCell.get(0));
                                }
                            }
                        }
                        p++;
                        if (p > isVn.length - 1) {
                            break;
                        }
                    }
                }
                FIRST.put(key + "â†’" + oneLeft, firstCellOne);
            }
            FIRST.put(key, firstCell);
            // è¾“å‡ºkeyçš„FIRSTé›†å�ˆ
            outText.append(
                    "\tFIRST(" + key + ")={" + String.join("ã€�", firstCell.toArray(new String[firstCell.size()])) + "}"+"\r\n");
        }
    }
    // æ±‚æ¯�ä¸ªé�žç»ˆç»“ç¬¦å�·çš„FLLOWé›†å�ˆ
    private void findFollow() {
        outText.append("\nFOLLOWé›†å�ˆ:"+"\r\n");
        Iterator<String> it = VN.iterator();
        HashMap<String, HashSet<String>> keyFollow = new HashMap<>();

        ArrayList<HashMap<String, String>> vn_VnList = new ArrayList<>();// ç”¨äºŽå­˜æ”¾/A->...B æˆ–è€… A->...BÎµçš„ç»„å�ˆ

        HashSet<String> vn_VnListLeft = new HashSet<>();// å­˜æ”¾vn_VnListçš„å·¦è¾¹å’Œå�³è¾¹
        HashSet<String> vn_VnListRight = new HashSet<>();
        // å¼€å§‹ç¬¦å�·åŠ å…¥#
        keyFollow.put(START, new HashSet<String>() {
            private static final long serialVersionUID = 1L;
            {
                add(new String("#"));
            }
        });

        while (it.hasNext()) {
            String key = it.next();
            ArrayList<ArrayList<String>> list = MAP.get(key);
            ArrayList<String> listCell;

            // å…ˆæŠŠæ¯�ä¸ªVNä½œä¸ºkeyFollowçš„keyï¼Œä¹‹å�Žåœ¨æŸ¥æ‰¾æ·»åŠ å…¶FOLLOWå…ƒç´ 
            if (!keyFollow.containsKey(key)) {
                keyFollow.put(key, new HashSet<>());
            }
            keyFollow.toString();

            for (int i = 0; i < list.size(); i++) {
                listCell = list.get(i);

                // (1)ç›´æŽ¥æ‰¾é�žæ€»ç»“ç¬¦å�·å�Žé�¢è·Ÿç�€ç»ˆç»“ç¬¦å�·
                for (int j = 1; j < listCell.size(); j++) {
                    HashSet<String> set = new HashSet<>();
                    if (VT.contains(listCell.get(j))) {
                        // System.out.println(listCell.get(j - 1) + ":" + listCell.get(j));
                        set.add(listCell.get(j));
                        if (keyFollow.containsKey(listCell.get(j - 1))) {
                            set.addAll(keyFollow.get(listCell.get(j - 1)));
                        }
                        keyFollow.put(listCell.get(j - 1), set);
                    }
                }
                // (2)æ‰¾...VnVn...ç»„å�ˆ
                for (int j = 0; j < listCell.size() - 1; j++) {
                    HashSet<String> set = new HashSet<>();
                    if (VN.contains(listCell.get(j)) && VN.contains(listCell.get(j + 1))) {
                        set.addAll(FIRST.get(listCell.get(j + 1)));
                        set.remove("Îµ");

                        if (keyFollow.containsKey(listCell.get(j))) {
                            set.addAll(keyFollow.get(listCell.get(j)));
                        }
                        keyFollow.put(listCell.get(j), set);
                    }
                }

                // (3)A->...B æˆ–è€… A->...BÎµ(å�¯ä»¥æœ‰nä¸ªÎµ)çš„ç»„å�ˆå­˜èµ·æ�¥
                for (int j = 0; j < listCell.size(); j++) {
                    HashMap<String, String> vn_Vn;
                    if (VN.contains(listCell.get(j)) && !listCell.get(j).equals(key)) {// æ˜¯VNä¸”Aä¸�ç­‰äºŽB
                        boolean isAllNull = false;// æ ‡è®°VNå�Žæ˜¯å�¦ä¸ºç©º
                        if (j + 1 < listCell.size()) {// å�³A->...BÎµ(å�¯ä»¥æœ‰nä¸ªÎµ)
                            for (int k = j + 1; k < listCell.size(); k++) {
                                if ((FIRST.containsKey(listCell.get(k)) ? FIRST.get(listCell.get(k)).contains("Îµ")
                                        : false)) {// å¦‚æžœå…¶å�Žé�¢çš„éƒ½æ˜¯VNä¸”å…¶FIRSTä¸­åŒ…å�«Îµ
                                    isAllNull = true;
                                } else {
                                    isAllNull = false;
                                    break;
                                }
                            }
                        }
                        // å¦‚æžœæ˜¯æœ€å�Žä¸€ä¸ªä¸ºVN,å�³A->...B
                        if (j == listCell.size() - 1) {
                            isAllNull = true;
                        }
                        if (isAllNull) {
                            vn_VnListLeft.add(key);
                            vn_VnListRight.add(listCell.get(j));

                            // å¾€vn_VnListä¸­æ·»åŠ ï¼Œåˆ†å­˜åœ¨å’Œä¸�å­˜åœ¨ä¸¤ç§�æƒ…å†µ
                            boolean isHaveAdd = false;
                            for (int x = 0; x < vn_VnList.size(); x++) {
                                HashMap<String, String> vn_VnListCell = vn_VnList.get(x);
                                if (!vn_VnListCell.containsKey(key)) {
                                    vn_VnListCell.put(key, listCell.get(j));
                                    vn_VnList.set(x, vn_VnListCell);
                                    isHaveAdd = true;
                                    break;
                                } else {
                                    // åŽ»é‡�
                                    if (vn_VnListCell.get(key).equals(listCell.get(j))) {
                                        isHaveAdd = true;
                                        break;
                                    }
                                    continue;
                                }
                            }
                            if (!isHaveAdd) {// å¦‚æžœæ²¡æœ‰æ·»åŠ ï¼Œè¡¨ç¤ºæ˜¯æ–°çš„ç»„å�ˆ
                                vn_Vn = new HashMap<>();
                                vn_Vn.put(key, listCell.get(j));
                                vn_VnList.add(vn_Vn);
                            }
                        }
                    }
                }
            }
        }

        keyFollow.toString();

        // (4)vn_VnListLeftå‡�åŽ»vn_VnListRight,å‰©ä¸‹çš„å°±æ˜¯å…¥å�£äº§ç”Ÿå¼�ï¼Œ
        vn_VnListLeft.removeAll(vn_VnListRight);
        Queue<String> keyQueue = new LinkedList<>();// ç”¨æ ˆæˆ–è€…é˜Ÿåˆ—éƒ½è¡Œ
        Iterator<String> itVnVn = vn_VnListLeft.iterator();
        while (itVnVn.hasNext()) {
            keyQueue.add(itVnVn.next());
        }
        while (!keyQueue.isEmpty()) {
            String keyLeft = keyQueue.poll();
            for (int t = 0; t < vn_VnList.size(); t++) {
                HashMap<String, String> vn_VnListCell = vn_VnList.get(t);
                if (vn_VnListCell.containsKey(keyLeft)) {
                    HashSet<String> set = new HashSet<>();
                    // åŽŸæ�¥çš„FOLLOWåŠ ä¸Šå·¦è¾¹çš„FOLLOW
                    if (keyFollow.containsKey(keyLeft)) {
                        set.addAll(keyFollow.get(keyLeft));
                    }
                    if (keyFollow.containsKey(vn_VnListCell.get(keyLeft))) {
                        set.addAll(keyFollow.get(vn_VnListCell.get(keyLeft)));
                    }
                    keyFollow.put(vn_VnListCell.get(keyLeft), set);
                    keyQueue.add(vn_VnListCell.get(keyLeft));

                    // ç§»é™¤å·²å¤„ç�†çš„ç»„å�ˆ
                    vn_VnListCell.remove(keyLeft);
                    vn_VnList.set(t, vn_VnListCell);
                }
            }
        }

        // æ­¤æ—¶keyFollowä¸ºå®Œæ•´çš„FOLLOWé›†
        FOLLOW = keyFollow;
        // æ‰“å�°FOLLOWé›†å�ˆ
        Iterator<String> itF = keyFollow.keySet().iterator();
        while (itF.hasNext()) {
            String key = itF.next();
            HashSet<String> f = keyFollow.get(key);
            outText.append("\tFOLLOW(" + key + ")={" + String.join("ã€�", f.toArray(new String[f.size()])) + "}"+"\r\n");
        }
    }
    // åˆ¤æ–­æ˜¯å�¦æ˜¯LL(1)æ–‡æ³•
    private boolean isLL1() {
        outText.append("\næ­£åœ¨åˆ¤æ–­æ˜¯å�¦æ˜¯LL(1)æ–‡æ³•...."+"\r\n");
        boolean flag = true;// æ ‡è®°æ˜¯å�¦æ˜¯LL(1)æ–‡æ³•
        Iterator<String> it = VN.iterator();
        while (it.hasNext()) {
            String key = it.next();
            ArrayList<ArrayList<String>> list = MAP.get(key);// å�•æ�¡äº§ç”Ÿå¼�
            if (list.size() > 1) { // å¦‚æžœå�•æ�¡äº§ç”Ÿå¼�çš„å·¦è¾¹åŒ…å�«ä¸¤ä¸ªå¼�å­�ä»¥ä¸Šï¼Œåˆ™è¿›è¡Œåˆ¤æ–­
                for (int i = 0; i < list.size(); i++) {
                    String aLeft = String.join("", list.get(i).toArray(new String[list.get(i).size()]));
                    for (int j = i + 1; j < list.size(); j++) {
                        String bLeft = String.join("", list.get(j).toArray(new String[list.get(j).size()]));
                        if ("Îµ".equals(aLeft) || "Îµ".equals(bLeft)) { // (1)è‹¥bï¼�Îµ,åˆ™è¦�FIRST(A)âˆ©FOLLOW(A)=Ï†
                            HashSet<String> retainSet = new HashSet<>();
                            // retainSet=FIRST.get(key);//éœ€è¦�è¦�æ·±æ‹·è´�ï¼Œå�¦åˆ™ä¿®æ”¹retainSetæ—¶FIRSTå�Œæ ·ä¼šè¢«ä¿®æ”¹
                            retainSet.addAll(FIRST.get(key));
                            if (FOLLOW.get(key) != null) {
                                retainSet.retainAll(FOLLOW.get(key));
                            }
                            if (!retainSet.isEmpty()) {
                                flag = false;// ä¸�æ˜¯LL(1)æ–‡æ³•ï¼Œè¾“å‡ºFIRST(a)FOLLOW(a)çš„äº¤é›†
                                outText.append("\tFIRST(" + key + ") âˆ© FOLLOW(" + key + ") = {"
                                        + String.join("ã€�", retainSet.toArray(new String[retainSet.size()])) + "}\r\n");
                                break;
                            } else {
                                outText.append("\tFIRST(" + key + ") âˆ© FOLLOW(" + key + ") = Ï†"+"\r\n");
                            }
                        } else { // (2)b!ï¼�Îµè‹¥,åˆ™è¦�FIRST(a)âˆ©FIRST(b)= Ð¤
                            HashSet<String> retainSet = new HashSet<>();
                            retainSet.addAll(FIRST.get(key + "â†’" + aLeft));
                            retainSet.retainAll(FIRST.get(key + "â†’" + bLeft));
                            if (!retainSet.isEmpty()) {
                                flag = false;// ä¸�æ˜¯LL(1)æ–‡æ³•ï¼Œè¾“å‡ºFIRST(a)FIRST(b)çš„äº¤é›†
                                outText.append("\tFIRST(" + aLeft + ") âˆ© FIRST(" + bLeft + ") = {"
                                        + String.join("ã€�", retainSet.toArray(new String[retainSet.size()])) + "}"+"\r\n");
                                break;
                            } else {
                                outText.append("\tFIRST(" + aLeft + ") âˆ© FIRST(" + bLeft + ") = Ï†"+"\r\n");
                            }
                        }
                    }
                }
            }
        }
        if(flag) {
            outText.append("\tæ˜¯LL(1)æ–‡æ³•,ç»§ç»­åˆ†æž�!"+"\r\n");
        }else {
            outText.append("\tä¸�æ˜¯LL(1)æ–‡æ³•,é€€å‡ºåˆ†æž�!"+"\r\n");
        }
        return flag;
    }
    // æž„å»ºé¢„æµ‹åˆ†æž�è¡¨FORM
    private void preForm() {
        HashSet<String> set = new HashSet<>();
        set.addAll(VT);
        set.remove("Îµ");
        FORM = new String[VN.size() + 1][set.size() + 2];
        Iterator<String> itVn = VN.iterator();
        Iterator<String> itVt = set.iterator();

        // (1)åˆ�å§‹åŒ–FORM,å¹¶æ ¹æ�®oneLeftFirst(VN$VT,äº§ç”Ÿå¼�)å¡«è¡¨
        for (int i = 0; i < FORM.length; i++){
            for (int j = 0; j < FORM[0].length; j++) {
                if (i == 0 && j > 0) {// ç¬¬ä¸€è¡Œä¸ºVt
                    if (itVt.hasNext()) {
                        FORM[i][j] = itVt.next();
                    }
                    if (j == FORM[0].length - 1) {// æœ€å�Žä¸€åˆ—åŠ å…¥#
                        FORM[i][j] = "#";
                    }
                }
                if (j == 0 && i > 0) {// ç¬¬ä¸€åˆ—ä¸ºVn
                    if (itVn.hasNext()) {
                        FORM[i][j] = itVn.next();
                    }
                }
                if (i > 0 && j > 0) {// å…¶ä»–æƒ…å†µå…ˆæ ¹æ�®oneLeftFirstå¡«è¡¨
                    String oneLeftKey = FORM[i][0] + "$" + FORM[0][j];// ä½œä¸ºkeyæŸ¥æ‰¾å…¶Firsté›†å�ˆ
                    FORM[i][j] = oneLeftFirst.get(oneLeftKey);
                }
            }
        }

        // (2)å¦‚æžœæœ‰æŽ¨å‡ºäº†Îµï¼Œåˆ™æ ¹æ�®FOLLOWå¡«è¡¨
        for (int i = 1; i < FORM.length; i++) {
            String oneLeftKey = FORM[i][0] + "$Îµ";
            if (oneLeftFirst.containsKey(oneLeftKey)) {
                HashSet<String> followCell = FOLLOW.get(FORM[i][0]);
                Iterator<String> it = followCell.iterator();
                while (it.hasNext()) {
                    String vt = it.next();
                    for (int j = 1; j < FORM.length; j++) {
                        for (int k = 1; k < FORM[0].length; k++) {
                            if (FORM[j][0].equals(FORM[i][0]) && FORM[0][k].equals(vt)) {
                                FORM[j][k] = oneLeftFirst.get(oneLeftKey);
                            }
                        }
                    }
                }
            }
        }

        // (3)æ‰“å�°é¢„æµ‹è¡¨,å¹¶å­˜äºŽMapçš„æ•°æ�®ç»“æž„ä¸­ç”¨äºŽå¿«é€ŸæŸ¥æ‰¾
        outText.append("\nè¯¥æ–‡æ³•çš„é¢„æµ‹åˆ†æž�è¡¨ä¸ºï¼š"+"\r\n");
        for (int i = 0; i < FORM.length; i++) {
            for (int j = 0; j < FORM[0].length; j++) {
                if (FORM[i][j] == null) {
                    outText.append(" " + "\t");
                }
                else {
                    outText.append(FORM[i][j] + "\t");
                    if (i > 0 && j > 0) {
                        String[] tmp = FORM[i][j].split("â†’");
                        preMap.put(FORM[i][0] + "" + FORM[0][j], tmp[1]);
                    }
                }
            }
            outText.append("\r\n");
        }
        outText.append("\r\n");
    }
    // è¾“å…¥çš„å�•è¯�ä¸²åˆ†æž�æŽ¨å¯¼è¿‡ç¨‹
    public void printAutoPre(String str) {
        outText.append(str + "çš„åˆ†æž�è¿‡ç¨‹:"+"\r\n");
        Queue<String> queue = new LinkedList<>();// å�¥å­�æ‹†åˆ†å­˜äºŽé˜Ÿåˆ—
        for (int i = 0; i < str.length(); i++) {
            String t = str.charAt(i) + "";
            if (i + 1 < str.length() && (str.charAt(i + 1) == '\'' || str.charAt(i + 1) == '’')) {
                t += str.charAt(i + 1);
                i++;
            }
            queue.offer(t);
        }
        queue.offer("#");// "#"ç»“æ�Ÿ
        // åˆ†æž�æ ˆ
        Stack<String> stack = new Stack<>();
        stack.push("#");// "#"å¼€å§‹
        stack.push(START);// åˆ�æ€�ä¸ºå¼€å§‹ç¬¦å�·
        boolean isSuccess = false;
        int step = 1;
        while (!stack.isEmpty()) {
            String left = stack.peek();
            String right = queue.peek();
            // (1)åˆ†æž�æˆ�åŠŸ
            if (left.equals(right) && "#".equals(right)) {
                isSuccess = true;
                outText.append((step++) + "\t#\t#\t" + "åˆ†æž�æˆ�åŠŸ"+"\r\n");
                break;
            }
            // (2)åŒ¹é…�æ ˆé¡¶å’Œå½“å‰�ç¬¦å�·ï¼Œå�‡ä¸ºç»ˆç»“ç¬¦å�·ï¼Œæ¶ˆåŽ»
            if (left.equals(right)) {
                String stackStr = String.join("", stack.toArray(new String[stack.size()]));
                String queueStr = String.join("", queue.toArray(new String[queue.size()]));
                outText.append((step++) + "\t" + stackStr + "\t" + queueStr + "\tåŒ¹é…�æˆ�åŠŸ" + left + "\r\n");
                stack.pop();
                queue.poll();
                continue;
            }
            // (3)ä»Žé¢„æµ‹è¡¨ä¸­æŸ¥è¯¢
            if (preMap.containsKey(left + right)) {
                String stackStr = String.join("", stack.toArray(new String[stack.size()]));
                String queueStr = String.join("", queue.toArray(new String[queue.size()]));
                outText.append((step++) + "\t" + stackStr + "\t" + queueStr + "\tç”¨" + left + "â†’"
                        + preMap.get(left + right) + "," + right + "é€†åº�è¿›æ ˆ" + "\r\n");
                stack.pop();
                String tmp = preMap.get(left + right);
                for (int i = tmp.length() - 1; i >= 0; i--) {// é€†åº�è¿›æ ˆ
                    String t = "";
                    if (tmp.charAt(i) == '\'' || tmp.charAt(i) == '’') {
                        t = tmp.charAt(i-1)+""+tmp.charAt(i);
                        i--;
                    }else {
                        t=tmp.charAt(i)+"";
                    }
                    if (!"Îµ".equals(t)) {
                        stack.push(t);
                    }
                }
                continue;
            }
            break;// (4)å…¶ä»–æƒ…å†µå¤±è´¥å¹¶é€€å‡º
        }
        if (!isSuccess) {
            outText.append((step++) + "\t#\t#\t" + "åˆ†æž�å¤±è´¥"+"\r\n");
        }
    }
}
