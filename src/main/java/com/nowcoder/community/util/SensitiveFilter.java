package com.nowcoder.community.util;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;

@Component //各层次通用的工具注解
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMEAN = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    // 该注解表示这是一个初始化方法，当容器实例化SensitiveFilter Bean以后，调用它的构造器之后，初始化方法会被自动地调用；
    // 那Bean什么时候初始化呢，服务启动的时候Bean就初始化了
    @PostConstruct
    public void init(){
        try(
                // 这句话加载后得到一个字节流
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 字节流中读文字不方便，需要转化成字符流，字符流效率低，再转成缓冲流，读取数据效率高
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        )
        {
            String keyWord;
            while((keyWord = reader.readLine()) != null){
                // 添加到前缀树
                this.addKeyword(keyWord);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    // 将一个敏感词加入到前缀树中
    private void addKeyword(String keyWord){
        TrieNode tempNode = rootNode;

        for(int i=0; i<keyWord.length(); i++){
            char c = keyWord.charAt(i);

            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode == null){
                // 初始化子结点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指向子结点，进入下一轮循环
            // 先下一轮，再设置结束，从定义类本身理解数据结构，每一个结点不包含数据，只有一个自己的标记和记录它下层结点的map，
            // 当赌这一层的下一层加入博时，创建了新结点，用key命名为博，随后再把这个结点的标记设置为False
            tempNode = subNode;

            // 设置结束标识
            if(i == keyWord.length()-1){
                tempNode.isKeywordEnd = true;
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder(); // 因为需要可变字符串，String是固定字符串，所以用StringBuilder

        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c); //以赌博为例，当c为赌这一层时，它的map里由key为博的结点，它的标记是true，
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                begin++;
                position = begin; // position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(REPLACEMEAN);
                // 进入下一个位置
                position++;
                begin = position; // begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 前缀树
    // 从这个定义类的本身理解数据结构，每一个结点不包含数据，只有一个自己的标记和记录它下层结点的map；
    // 用map的key和value之间的关系来模拟树各个结点之间的关系，一种新的定义树的方式
    private static class TrieNode{

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子结点(key是下级字符，value是下级结点)
        HashMap<Character, TrieNode> subNode = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子结点
        public void addSubNode(Character c, TrieNode trieNode){
            subNode.put(c, trieNode);
        }

        // 获取子结点
        public TrieNode getSubNode(Character c){
            return subNode.get(c);
        }
    }
}


