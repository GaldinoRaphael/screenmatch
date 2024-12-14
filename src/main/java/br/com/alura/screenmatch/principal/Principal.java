package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporadas;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    public void exibeMenu(){
        System.out.println("Digite o nome da série para buscar");
        var nomeSerie = leitura.nextLine();
        var endereco = ENDERECO + nomeSerie.replace(" ", "+") + API_KEY;
        var json = consumo.obterDados(endereco);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporadas> temporadas = new ArrayList<>();

        for(int i = 1; i<=dados.totalTemporadas(); i++) {
            json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") +"&season=" + i + API_KEY);
            DadosTemporadas dadosTemporada = conversor.obterDados(json, DadosTemporadas.class);
            temporadas.add(dadosTemporada);

        }

        List<DadosEpisodio> dadosEpisodios = temporadas
                .stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

        dadosEpisodios
                .stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

//        System.out.println("Digite apartir que ano deseja filtrar: ");
//        int ano = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                   .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                " Episódio: " + e.getTitulo() +
//                                " Data lançamento: " + e.getDataLancamento().format(formatador)
//                    ));

//        System.out.println("Digite o titulo do episódio: ");
//        var trechoTitulo = leitura.nextLine();
//
//        Optional<Episodio> episodio = episodios.stream().filter(e -> e.getTitulo().toLowerCase().contains(trechoTitulo.toLowerCase())).findFirst();
//
//        if(episodio.isPresent()){
//            System.out.println(episodio.get());
//        }else{
//            System.out.println("Não encontrado!");
//        }

        Map<Integer, Double> mediaPorTemporada = episodios.stream().filter(e -> e.getAvaliacao() > 0).collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(mediaPorTemporada);


        DoubleSummaryStatistics est = episodios.stream().filter(episodio -> episodio.getAvaliacao() > 0).collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Melhor avaliação: " + est.getMax());
        System.out.println("menor avaliação: " + est.getMin());
    }

}
