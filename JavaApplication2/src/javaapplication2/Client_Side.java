
package javaapplication2;
import java.util.*;
import java.io.*;
import java.net.*;

public class Client_Side implements Runnable{
    public int porta = 10000;
    Socket server;  //declara socket de conexão com servidor
    
    public Client_Side(){
        new Thread(this).start();   //inicia o processo.
        System.out.println("Thread Pronta!");
    }
    public static void main(String[] args) throws UnknownHostException, IOException {
        Client_Side cliente = new Client_Side();    //chama a classe cliente (inicia)
    }

    @Override
    public void run() {
            try {
                server = new Socket("127.0.0.1", porta);    //inicializa o socket
                new Ouvir(server).start();  //inicia a thread para ouvir o servidor sempre que este mandar o buffer de saidad
                new Escrever(server).start();//inicia a trhead para escrever no servidor sempre que o cliente escrever algo
            } catch (IOException ex) {}
    }
}

class Ouvir extends Thread{
    Scanner entrada;    //scanner
    
    public Ouvir(Socket sok) throws IOException{
        entrada = new Scanner(sok.getInputStream());    //o scanner agora está linkado à entrada de dados provenientes do servidor.
    }
    
    @Override
    public void run() {
        while(true){    //sempre ouvir o servidodr
            if(entrada.hasNext()){  //enquanto o buffer ter algo
                System.out.println(entrada.nextLine()); //mostrar na tela do usuário
            }
        }
    }
    
}

class Escrever extends Thread{
    PrintStream Saida;  //buffer prinstream para se enviar dados ao servidor
    Scanner Teclado;    //scanner que lê dados do teclado
    
    public Escrever(Socket sok) throws IOException{
        Teclado = new Scanner(System.in);   //ler teclado
        Saida = new PrintStream(sok.getOutputStream()); //enviar para o servidor
    }
    
    @Override
    public void run() {
        while(true){
            if(Teclado.hasNextInt()){   //se o teclado escre algo
                Saida.println(Teclado.nextInt());   //enviar para o servidor a mensagem escrita 
            }
        }
    }
}