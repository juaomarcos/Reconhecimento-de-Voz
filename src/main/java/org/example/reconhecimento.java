package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;

public class reconhecimento {

    private JFrame janela;
    private JLabel resultado;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new reconhecimento().criarGUI());
    }

    private void criarGUI() {
        janela = new JFrame("App de Processamento de Áudio");
        janela.setSize(400, 300);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setLayout(new CardLayout());

        JPanel telaInicial = new JPanel();
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

        JPanel telaPrincipal = new JPanel();
        telaPrincipal.setLayout(new BoxLayout(telaPrincipal, BoxLayout.Y_AXIS));

        JLabel tituloPrincipal = new JLabel("");
        tituloPrincipal.setFont(new Font("Arial", Font.PLAIN, 18));
        tituloPrincipal.setAlignmentX(Component.CENTER_ALIGNMENT);
        telaPrincipal.add(Box.createRigidArea(new Dimension(0, 10)));
        telaPrincipal.add(tituloPrincipal);

        telaPrincipal.add(Box.createVerticalGlue());
        JButton botaoAudio = new JButton(" Começar reconhecimento ");
        botaoAudio.setAlignmentX(Component.CENTER_ALIGNMENT);
        botaoAudio.addActionListener(e -> pegarAudio());
        telaPrincipal.add(botaoAudio);

        JButton botaoArquivo = new JButton(" Encaminhar Arquivo de Áudio ");
        botaoArquivo.setAlignmentX(Component.CENTER_ALIGNMENT);
        botaoArquivo.addActionListener(e -> carregarArquivoAudio());
        telaPrincipal.add(Box.createRigidArea(new Dimension(0,  4)));
        telaPrincipal.add(botaoArquivo);
        telaPrincipal.add(Box.createVerticalGlue());

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

        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try (TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info)) {
                    line.open(format);
                    line.start();

                    try (Model model = new Model(modelPath);
                         Recognizer recognizer = new Recognizer(model, 16000.0f)) {

                        publish("Reconhecimento iniciado... Fale algo!");

                        byte[] buffer = new byte[4096];
                        while (true) {
                            int bytesRead = line.read(buffer, 0, buffer.length);
                            if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                                String result = recognizer.getResult();
                                publish("Texto reconhecido: " + result);
                                break;
                            } else {
                                String partial = recognizer.getPartialResult();
                                publish("Texto parcial: " + partial);
                            }
                        }
                    }
                } catch (LineUnavailableException e) {
                    publish("Erro: Microfone não disponível.");
                } catch (IOException e) {
                    publish("Erro: Problema ao carregar o modelo.");
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    resultado.setText("<html>" + chunk.replace("\n", "<br>") + "</html>");
                }
            }
        }.execute();
    }

    private void carregarArquivoAudio() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arquivos de Áudio", "mp3", "wav");
        fileChooser.setFileFilter(filter);
        int resultadoEscolha = fileChooser.showOpenDialog(janela);

        if (resultadoEscolha == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            resultado.setText("Carregando áudio: " + arquivo.getAbsolutePath());

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                }

                @Override
                protected void done() {
                    resultado.setText("Áudio carregado e processado: " + arquivo.getAbsolutePath());
                }
            }.execute();
        } else {
            resultado.setText("Nenhum arquivo carregado.");
        }
    }

}
