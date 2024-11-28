package org.example;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;

public class AudioApp {

    private JFrame janela;
    private JPanel telaInicial, telaPrincipal;
    private JLabel resultado;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AudioApp().criarGUI());
    }


    private void criarGUI() {
        janela = new JFrame("App de Processamento de Áudio");
        janela.setSize(400, 300);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setLayout(new CardLayout());

        telaInicial = new JPanel();
        telaInicial.setLayout(new BoxLayout(telaInicial, BoxLayout.Y_AXIS));

        JLabel tituloInicial = new JLabel("Bem-vindo ao App");
        tituloInicial.setFont(new Font("Arial", Font.PLAIN, 18));
        tituloInicial.setAlignmentX(Component.CENTER_ALIGNMENT);
        telaInicial.add(Box.createRigidArea(new Dimension(0, 50)));
        telaInicial.add(tituloInicial);

        JButton botaoAvancar = new JButton("Avançar");
        botaoAvancar.setAlignmentX(Component.CENTER_ALIGNMENT);
        botaoAvancar.addActionListener(e -> avancarParaPrincipal());
        telaInicial.add(botaoAvancar);

        telaPrincipal = new JPanel();
        telaPrincipal.setLayout(new BoxLayout(telaPrincipal, BoxLayout.Y_AXIS));

        JLabel tituloPrincipal = new JLabel("Tela Principal");
        tituloPrincipal.setFont(new Font("Arial", Font.PLAIN, 18));
        tituloPrincipal.setAlignmentX(Component.CENTER_ALIGNMENT);
        telaPrincipal.add(Box.createRigidArea(new Dimension(0, 10)));
        telaPrincipal.add(tituloPrincipal);

        JButton botaoAudio = new JButton("Pegar Áudio");
        botaoAudio.setAlignmentX(Component.CENTER_ALIGNMENT);
        botaoAudio.addActionListener(e -> pegarAudio());
        telaPrincipal.add(botaoAudio);

        JButton botaoArquivo = new JButton("Encaminhar Arquivo de Áudio");
        botaoArquivo.setAlignmentX(Component.CENTER_ALIGNMENT);
        botaoArquivo.addActionListener(e -> carregarArquivoAudio());
        telaPrincipal.add(botaoArquivo);

        resultado = new JLabel("");
        resultado.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultado.setPreferredSize(new Dimension(350, 100));
        telaPrincipal.add(Box.createRigidArea(new Dimension(0, 20)));
        telaPrincipal.add(resultado);

        janela.add(telaInicial, "Tela Inicial");
        janela.add(telaPrincipal, "Tela Principal");

        janela.setVisible(true);
    }

    private void avancarParaPrincipal() {
        CardLayout cl = (CardLayout) janela.getContentPane().getLayout();
        cl.show(janela.getContentPane(), "Tela Principal");
    }

    private void pegarAudio() {
        String modelPath = "src/main/resources/model";
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        new Thread(() -> {
            try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();

                try (Model model = new Model(modelPath);
                     Recognizer recognizer = new Recognizer(model, 16000.0f)) {

                    SwingUtilities.invokeLater(() -> resultado.setText("Reconhecimento iniciado... Fale algo!"));

                    byte[] buffer = new byte[4096];
                    while (true) {
                        int bytesRead = line.read(buffer, 0, buffer.length);
                        if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                            String result = recognizer.getResult();
                            SwingUtilities.invokeLater(() -> resultado.setText("<html>" + result.replace("\n", "<br>") + "</html>"));
                            break;
                        } else {
                            String partial = recognizer.getPartialResult();
                            SwingUtilities.invokeLater(() -> resultado.setText("<html>" + partial.replace("\n", "<br>") + "</html>"));
                        }
                    }
                }
            } catch (LineUnavailableException e) {
                SwingUtilities.invokeLater(() -> resultado.setText("Erro: Microfone não disponível."));
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> resultado.setText("Erro: Problema ao carregar o modelo."));
            }
        }).start();
    }

    private void carregarArquivoAudio() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos de Áudio", "mp3", "wav");
        fileChooser.setFileFilter(filter);
        int resultadoEscolha = fileChooser.showOpenDialog(janela);

        if (resultadoEscolha == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            resultado.setText("Áudio carregado: " + arquivo.getAbsolutePath() + "\nFunção para processar áudio em desenvolvimento.");
        } else {
            resultado.setText("Nenhum arquivo carregado.");
        }
    }
}

