/*
    Segurança: as entradas dos jogadores não estão sendo validadas
*/


package javaapplication2;
import java.net.*;
import java.io.*;
import java.util.*;

public class Server_Side implements Runnable {
    public int porta = 10000;
    ServerSocket Server;
    
    public Server_Side() throws Exception {
        Server = new ServerSocket(this.porta); //instancia o serversocket na porta passada por parâmetro
        new Thread(this).start();   //inicia a thread do lado servidor para atender multiplos clientes
        System.out.println("Servidor ouvindo na porta:" + porta);
    }

    @Override
    public void run() {
        try {
            while (true) {
                new JogoDoido(Server.accept()).start();//aceitando conexão com cliente e iniciando uma thread própria para este
            }
        }                                                                                                                                   catch (IOException e) {System.exit(1);}
    }

    public static void main(String[] args) {
        try {
            Server_Side server = new Server_Side();   //inicializa a aplicação
        }                                                                                                                                   catch (Exception e) {System.exit(1);}
    }
}

class JogoDoido extends Thread {
    
    private Socket client;  //o cliente sendo representado como socket(conexão)
    private static int vencedor = 0;   //variavel que guarda o id do vencedor do jogo
    private int id_jogador = 0;  //atributo para marcar qual o atual jogador
    private int num_palitos_jogador;    //numero de palitos do jogador
    private static int ready = 0;    //variavel de controle de quantos jogadores já terminaram de apostar
    private static int max = 10;
    private static int num_jogadores = 0; //numero total de jogadores conectados como static para se diferenciar entre as threads
    private static int[] jogada = new int[max];   //vetor para armazenar as respostas dos jogadores com indice igual ao numero do jogador
    private static int[] aposta = new int[max];    //vetor para armazenar as apostas dos jogadores

    public JogoDoido(Socket s) {
        num_jogadores++;  //incrementa o numero total de jogadores presentes na sala do jogo
        id_jogador = num_jogadores;    //marca qual o numero do jogador atual na sala do jogo
        num_palitos_jogador = 3;    //inicializa o numero de palitos como 3 no inicio do jogo
        client = s; //O socket nomeado client
    }

    @Override
    public void run() {
        try {
            System.out.println("Jogador "+id_jogador+" conectado!");   //Notificação ao servidor que mais um se conectou
            
            PrintStream Saida = new PrintStream(client.getOutputStream()); //Cria um Buffer de saida de dados
            Scanner Entrada = new Scanner(client.getInputStream());//Cria um Bufferde leitura de dados
            
            Saida.println("Seu ID:"+id_jogador);   //cumprimentos do cliente
            
            while(vencedor == 0){ //enquanto não ouverem vencedores
                Saida.println("===============Nova Rodada===============");
//1-Informa quantidade de palitos, requisita a jogada e requisita a aposta:
                Saida.println("Numero de palitos disponíveis:"+num_palitos_jogador+"\nInforme a jogada:");
                if(Entrada.hasNextInt()){jogada[id_jogador] = Entrada.nextInt();}  //grava a jogada do jogador dentro do vetor no indice de seu número de sala
                Saida.println("Informe sua aposta:");
                if(Entrada.hasNextInt()){aposta[id_jogador] = Entrada.nextInt();}
                
                //log do Servidor
                System.out.println("MOVE: "+id_jogador+": jogo: "+jogada[id_jogador]+"| aposta:"+aposta[id_jogador]); //mostra a resposta do jogador para o servidor
                
//2-Aguardar até todos os jogadores fazerem as jogadas e apostas
                Saida.println("Aguardando Outros jogadores..");  //informa ao jogador que outros ainda estão apostando
                ready++;    //sinal de sincornização de threads
                synchronized(JogoDoido.class){  //metodo sincronizado entre as threads para usar o wait() e notifyAll()
                    if(ready != num_jogadores){JogoDoido.class.wait();} //caso nem todos tenham terminado a jogada, a thread espera
                    else{JogoDoido.class.notifyAll();}  //se todos já terminaram, a thread avisa todas as outras para acordarem do wait() e continuar
                }
                
//5-Definir o resultado
                //definir o total de palitos na rodada
                int Soma_total = 0; //incializa a variavel que representa o total de palitos na rodada
                for(int y=1;y<=num_jogadores;y++){ 
                    Soma_total += jogada[y];    //adiciona o numero de palitos jogados na rodada
                }
                
                //definir o numero de ganhadores
                int num_ganhadores = 0; //variavel que armazenará numero de ganhadores da rodada
                int vencedor = 0;   //variavel que armazena o vencedor do jogo
                for(int z=1;z<=num_jogadores;z++){ 
                    if(aposta[z] == Soma_total){   //verifica se o jogador acertou a aposta
                        num_ganhadores++;    //adiciona o numero de palitos jogados na rodada
                        Saida.println("ACERTOU: "+z);
                        
                        if(id_jogador == z){num_palitos_jogador--;} //subtrair um palito se o jogador acertou
                        
                        if(id_jogador == z && num_palitos_jogador <= 0){ //se o jogador (thread atual) não tem mais palitos (ganhou o jogo)
                            this.vencedor = z; 
                        }
                    }
                    else{
                        Saida.println("ERROU: "+z);
                    }
                }
                if(num_ganhadores == 0){Saida.println("Não há ganhadores da rodada!");} //se ninguem vencer a rodada
                ready = 0;  //reinicia a variavel de sinal para uma nova rodada começar
            }
              Saida.println("O JOGADOR "+vencedor+" GANHOU O JOGO");
              System.exit(0);
        }                                                                                                                                   catch (IOException e) {System.exit(1);} catch (InterruptedException ex) {
        }
    }
}