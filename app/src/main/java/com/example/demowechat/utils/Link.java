package com.example.demowechat.utils;

import java.util.ArrayList;

public class Link<T>{
    private class Node {
        private T data;
        private Node next;

        public Node(T data) {
            this.data = data;
        }

        public void addNode(Node node) {
            if (this.next == null) {
                this.next=node;
            } else {
                this.next.addNode(node);
            }
        }

        public void printNode() {
            System.out.println(this.data);
            if (this.next != null) {
                this.next.printNode();
            }
        }
        public T getNode(int index){
            if(index==Link.this.foot){
            	
            	Link.this.foot = 0;
                return this.data;
            }else{
            	++Link.this.foot;
            	/*System.out.println(index);
            	System.out.println(Link.this.foot);*/
                return this.next.getNode(index);
            }
        }
        public boolean containsNode(T data){
            if(data==this.data){
                return true;
            }else {
                if(this.next!=null){
                    return this.next.containsNode(data);
                }else{
                    return false;
                }

            }
        }
        public void removeNode(Node previous,T data){
            if(this.data.equals(data)){
                previous.next=this.next;
            }else {
                this.next.removeNode(this,data);
            }
        }
        public void toArrayNode(){
            Link.this.retData.add(this.data);
            //Link.this.retData[Link.this.foot++]=this.data;
            if(this.next!=null){
                this.next.toArrayNode();
            }

        }
    }
    private Node root;
    private int count=0;
    private int foot = 0;
    private ArrayList<T> retData;//����һ��ArrayList<T>����������ȫ������
    public void add(T data){//����������������
        Node newNode=new Node(data);
        if(data==null) return;

        if(this.root==null){
            this.root=newNode;
        }else{
            this.root.addNode(newNode);
        }
        this.count++;
    }
    public void print(){
        if(this.root!=null){
            this.root.printNode();
        }
    }
    public int size(){//����Ĵ�С
        return this.count;
    }
    public boolean isEmpty(){//�ж��Ƿ�Ϊ������
        return this.count==0;
    }
    public void clean(){
        this.root=null;
        this.count=0;
    }
    public T get(int index){
        if(index>this.count){
            return null;
        }else{
            return this.root.getNode(index);
        }

    }
    public boolean contains(T data){
        if(data==null){
            return false;
        }
        return this.root.containsNode(data);
    }
    public T remove(T data){
        if(this.contains(data)){
            if(this.root.data==data){
                this.root=this.root.next;
            }else {
                this.root.next.removeNode(this.root,data);
            }
            this.count--;
            return data;
        }
        return null;
    }
    
    public T remove(int index) {
    	return remove(get(index));
    }
    
    
    public ArrayList<T> toArray(){
        if(this.count==0){
            return null;
        }
        this.foot=0;
        this.retData=new ArrayList<T>(this.count);
        this.root.toArrayNode();
        return this.retData;
    }
}

