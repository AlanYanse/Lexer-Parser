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
                    isVn[0] = true;// The first is a non terminal
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
                                if (VT.contains(listGoCell.get(0))) { // if the first character is a terminator
                                    if ("Îµ".equals(listGoCell.get(0))) {
                                        if (!key.equals(START)) { // start symbol cannot be pushed empty
                                            firstCell.add(listGoCell.get(0));
                                            firstCellOne.add(listGoCell.get(0));
                                            oneLeftFirst.put(key + "$" + listGoCell.get(0), key + "â†’" + oneLeft);
                                        }
                                        if (p + 1 < isVn.length) {// If empty, you can query the next character
                                            isVn[p + 1] = true;
                                        }
                                    } else { // Non-empty terminal symbols are added to the corresponding FIRST set
                                        firstCell.add(listGoCell.get(0));
                                        firstCellOne.add(listGoCell.get(0));
                                        oneLeftFirst.put(key + "$" + listGoCell.get(0), key + "â†’" + oneLeft);
                                    }
                                } else {// Not a terminal symbol, push the stack
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
            // FIRST set of output keys
            outText.append(
                    "\tFIRST(" + key + ")={" + String.join("ã€�", firstCell.toArray(new String[firstCell.size()])) + "}"+"\r\n");
        }
    }
    // Find the FLLOW set of each nonterminal
    private void findFollow() {
        outText.append("\nFOLLOWé›†å�ˆ:"+"\r\n");
        Iterator<String> it = VN.iterator();
        HashMap<String, HashSet<String>> keyFollow = new HashMap<>();

        ArrayList<HashMap<String, String>> vn_VnList = new ArrayList<>();// Used to store combinations of /A->...B or A->...Bε

        HashSet<String> vn_VnListLeft = new HashSet<>();// Store the left and right sides of vn_VnList
        HashSet<String> vn_VnListRight = new HashSet<>();
        // start symbol join #
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

            // First use each VN as the key of keyFollow, and then add its FOLLOW element in the search
            if (!keyFollow.containsKey(key)) {
                keyFollow.put(key, new HashSet<>());
            }
            keyFollow.toString();

            for (int i = 0; i < list.size(); i++) {
                listCell = list.get(i);

                // (1) Directly find the non-summary symbol followed by the terminal symbol
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
                // (2) Find...VnVn...Combination
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

                // (3) A->...B or A->...Bε (there can be n ε) combinations are stored
                for (int j = 0; j < listCell.size(); j++) {
                    HashMap<String, String> vn_Vn;
                    if (VN.contains(listCell.get(j)) && !listCell.get(j).equals(key)) {// is VN and A is not equal to B
                        boolean isAllNull = false;// Is it empty after marking the VN
                        if (j + 1 < listCell.size()) {// That is, A->...B Ε (there can be n ε)
                            for (int k = j + 1; k < listCell.size(); k++) {
                                if ((FIRST.containsKey(listCell.get(k)) ? FIRST.get(listCell.get(k)).contains("Îµ")
                                        : false)) {// If it is followed by VN and its FIRST contains ε
                                    isAllNull = true;
                                } else {
                                    isAllNull = false;
                                    break;
                                }
                            }
                        }
                        // If the last one is VN, ie A->...B
                        if (j == listCell.size() - 1) {
                            isAllNull = true;
                        }
                        if (isAllNull) {
                            vn_VnListLeft.add(key);
                            vn_VnListRight.add(listCell.get(j));

                            // Add to vn_VnList, there are two cases: existence and non-existence
                            boolean isHaveAdd = false;
                            for (int x = 0; x < vn_VnList.size(); x++) {
                                HashMap<String, String> vn_VnListCell = vn_VnList.get(x);
                                if (!vn_VnListCell.containsKey(key)) {
                                    vn_VnListCell.put(key, listCell.get(j));
                                    vn_VnList.set(x, vn_VnListCell);
                                    isHaveAdd = true;
                                    break;
                                } else {
                                    // deduplication
                                    if (vn_VnListCell.get(key).equals(listCell.get(j))) {
                                        isHaveAdd = true;
                                        break;
                                    }
                                    continue;
                                }
                            }
                            if (!isHaveAdd) {// If it is not added, it means it is a new combination
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

        // (4) vn_VnListLeft minus vn_VnListRight, the rest is the entry production,
        vn_VnListLeft.removeAll(vn_VnListRight);
        Queue<String> keyQueue = new LinkedList<>();// Either stack or queue can be used
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
                    // The original FOLLOW plus the left FOLLOW
                    if (keyFollow.containsKey(keyLeft)) {
                        set.addAll(keyFollow.get(keyLeft));
                    }
                    if (keyFollow.containsKey(vn_VnListCell.get(keyLeft))) {
                        set.addAll(keyFollow.get(vn_VnListCell.get(keyLeft)));
                    }
                    keyFollow.put(vn_VnListCell.get(keyLeft), set);
                    keyQueue.add(vn_VnListCell.get(keyLeft));

                    // Remove processed combinations
                    vn_VnListCell.remove(keyLeft);
                    vn_VnList.set(t, vn_VnListCell);
                }
            }
        }

        // At this point keyFollow is the complete FOLLOW set
        FOLLOW = keyFollow;
        // print the FOLLOW collection
        Iterator<String> itF = keyFollow.keySet().iterator();
        while (itF.hasNext()) {
            String key = itF.next();
            HashSet<String> f = keyFollow.get(key);
            outText.append("\tFOLLOW(" + key + ")={" + String.join("ã€�", f.toArray(new String[f.size()])) + "}"+"\r\n");
        }
    }
    // Determine whether it is an LL(1) grammar
    private boolean isLL1() {
        outText.append("\næ­£åœ¨åˆ¤æ–­æ˜¯å�¦æ˜¯LL(1)æ–‡æ³•...."+"\r\n");
        boolean flag = true;// æ ‡è®°æ˜¯å�¦æ˜¯LL(1)æ–‡æ³•
        Iterator<String> it = VN.iterator();
        while (it.hasNext()) {
            String key = it.next();
            ArrayList<ArrayList<String>> list = MAP.get(key);// single production
            if (list.size() > 1) { // If the left side of a single production contains more than two expressions, make a judgment
                for (int i = 0; i < list.size(); i++) {
                    String aLeft = String.join("", list.get(i).toArray(new String[list.get(i).size()]));
                    for (int j = i + 1; j < list.size(); j++) {
                        String bLeft = String.join("", list.get(j).toArray(new String[list.get(j).size()]));
                        if ("Îµ".equals(aLeft) || "Îµ".equals(bLeft)) { // (1) If b=ε, then FIRST(A)∩FOLLOW(A)=φ
                            HashSet<String> retainSet = new HashSet<>();
                            // retainSet=FIRST.get(key);//A deep copy is required, otherwise the FIRST will also be modified when the retainSet is modified
                            retainSet.addAll(FIRST.get(key));
                            if (FOLLOW.get(key) != null) {
                                retainSet.retainAll(FOLLOW.get(key));
                            }
                            if (!retainSet.isEmpty()) {
                                flag = false;// Not LL(1) grammar, output the intersection of FIRST(a)FOLLOW(a)
                                outText.append("\tFIRST(" + key + ") âˆ© FOLLOW(" + key + ") = {"
                                        + String.join("ã€�", retainSet.toArray(new String[retainSet.size()])) + "}\r\n");
                                break;
                            } else {
                                outText.append("\tFIRST(" + key + ") âˆ© FOLLOW(" + key + ") = Ï†"+"\r\n");
                            }
                        } else { // (2) If b!=ε, then FIRST(a)∩FIRST(b)=Ф
                            HashSet<String> retainSet = new HashSet<>();
                            retainSet.addAll(FIRST.get(key + "â†’" + aLeft));
                            retainSet.retainAll(FIRST.get(key + "â†’" + bLeft));
                            if (!retainSet.isEmpty()) {
                                flag = false;// Not LL(1) grammar, output the intersection of FIRST(a)FIRST(b)
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
    // Build a predictive analysis table FORM
    private void preForm() {
        HashSet<String> set = new HashSet<>();
        set.addAll(VT);
        set.remove("Îµ");
        FORM = new String[VN.size() + 1][set.size() + 2];
        Iterator<String> itVn = VN.iterator();
        Iterator<String> itVt = set.iterator();

        // (1) Initialize FORM, and fill in the form according to oneLeftFirst(VN$VT, production)
        for (int i = 0; i < FORM.length; i++){
            for (int j = 0; j < FORM[0].length; j++) {
                if (i == 0 && j > 0) {// ç¬¬ä¸€è¡Œä¸ºVt
                    if (itVt.hasNext()) {
                        FORM[i][j] = itVt.next();
                    }
                    if (j == FORM[0].length - 1) {// The last column is added#
                        FORM[i][j] = "#";
                    }
                }
                if (j == 0 && i > 0) {// The first column is Vn
                    if (itVn.hasNext()) {
                        FORM[i][j] = itVn.next();
                    }
                }
                if (i > 0 && j > 0) {// In other cases, fill in the form according to oneLeftFirst
                    String oneLeftKey = FORM[i][0] + "$" + FORM[0][j];// Find its First collection as key
                    FORM[i][j] = oneLeftFirst.get(oneLeftKey);
                }
            }
        }

        // (2) If ε is introduced, fill in the form according to FOLLOW
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

        // (3) Print the prediction table and store it in the Map data structure for quick search
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
    // Input word string analysis and derivation process
    public void printAutoPre(String str) {
        outText.append(str + "çš„åˆ†æž�è¿‡ç¨‹:"+"\r\n");
        Queue<String> queue = new LinkedList<>();// Sentence splits are stored in the queue
        for (int i = 0; i < str.length(); i++) {
            String t = str.charAt(i) + "";
            if (i + 1 < str.length() && (str.charAt(i + 1) == '\'' || str.charAt(i + 1) == '’')) {
                t += str.charAt(i + 1);
                i++;
            }
            queue.offer(t);
        }
        queue.offer("#");// "#"Finish
        // åˆ†æž�æ ˆ
        Stack<String> stack = new Stack<>();
        stack.push("#");// "#"start
        stack.push(START);// The initial state is the start symbol
        boolean isSuccess = false;
        int step = 1;
        while (!stack.isEmpty()) {
            String left = stack.peek();
            String right = queue.peek();
            // (1) Analysis is successful
            if (left.equals(right) && "#".equals(right)) {
                isSuccess = true;
                outText.append((step++) + "\t#\t#\t" + "åˆ†æž�æˆ�åŠŸ"+"\r\n");
                break;
            }
            // (2) Match the top of the stack and the current symbol, both of which are terminal symbols, eliminate
            if (left.equals(right)) {
                String stackStr = String.join("", stack.toArray(new String[stack.size()]));
                String queueStr = String.join("", queue.toArray(new String[queue.size()]));
                outText.append((step++) + "\t" + stackStr + "\t" + queueStr + "\tåŒ¹é…�æˆ�åŠŸ" + left + "\r\n");
                stack.pop();
                queue.poll();
                continue;
            }
            // (3) Query from the prediction table
            if (preMap.containsKey(left + right)) {
                String stackStr = String.join("", stack.toArray(new String[stack.size()]));
                String queueStr = String.join("", queue.toArray(new String[queue.size()]));
                outText.append((step++) + "\t" + stackStr + "\t" + queueStr + "\tç”¨" + left + "â†’"
                        + preMap.get(left + right) + "," + right + "é€†åº�è¿›æ ˆ" + "\r\n");
                stack.pop();
                String tmp = preMap.get(left + right);
                for (int i = tmp.length() - 1; i >= 0; i--) {// push the stack in reverse order
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
            break;// (4) other cases fail and exit
        }
        if (!isSuccess) {
            outText.append((step++) + "\t#\t#\t" + "åˆ†æž�å¤±è´¥"+"\r\n");
        }
    }
}
