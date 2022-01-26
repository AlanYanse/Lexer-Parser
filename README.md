# :eyes:Lexer-Parser
Small program for lexical analysis and syntax analysis implemented in java:coffee:

## Overview
- **Lexical Analysis:** According to the input character sequence, convert the character sequence into a word `Token` sequence, identify each character, and give the corresponding type
- **Syntax analysis:** According to the given grammar, judge whether it is `LL(1) grammar`, and analyze it from top to bottom. Predictive analysis method is adopted: starting from the grammar start character S, scan the source program from left to right, look forward 1 character at a time, select the appropriate production, and generate the leftmost derivation of the sentence.
- **step:**   
1. Automatically distinguish between terminal `VT` and non-terminal `VN` when reading a grammar from a file
2. Eliminate direct left recursion
3. Generate `FIRST` and `FOLLOW` collections
4. Check if it is an `LL(1)` grammar
5. Build a predictive analytics table
6. Input the word string to be analyzed and automatically output the analysis process



## :tv:result graph
- **lexical analysis**

![insert image description here](https://img-blog.csdnimg.cn/20200611141000270.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)
- **Parsing**

![insert image description here](https://img-blog.csdnimg.cn/20200611141009521.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)

![insert image description here](https://img-blog.csdnimg.cn/20200611141013882.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)

- **Determine whether it is an LL(1) grammar**
 
![insert image description here](https://img-blog.csdnimg.cn/20200611141023906.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)




## code structure

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200610162702789.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)


## Instructions for use

- Put the `grammer` folder under the F drive, I also attached a file called `grammer test.txt` in the code package, which contains the corresponding four grammars and the string examples that need to be input.
- Run the Main main function, you can...

![insert image description here](https://img-blog.csdnimg.cn/20200611141035997.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80NDg2MTM5OQ==,size_16,color_FFFFFF,t_70)






