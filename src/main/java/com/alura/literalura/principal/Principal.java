package com.alura.literalura.principal;

import com.alura.literalura.model.*;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;


import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository repositoryLibro;
    private AutorRepository repositoryAutor;
    private List<Autor> autores;
    private List<Libro> libros;

    public Principal(LibroRepository repositoryLibro, AutorRepository repositoryAutor) {
        this.repositoryLibro = repositoryLibro;
        this.repositoryAutor = repositoryAutor;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            System.out.println("_____ BIENVENIDOS  A LITER ALURA-------------\n");
            var menu = """
                    1 - Buscar libros por título
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en determinado año
                    5 - Listar libros por idioma
                    
                    [[[[[[[[ ---EXTRA-- ]]]]]]]]
                    6 - Top 10 libros mas descargados
                    7 - Libro más descargado y menos descargado 
                    0 - Salir
                    TU OPCION: 
                    """;


            System.out.println(menu);
            while (!teclado.hasNextInt()) {
                System.out.println("OPCION INVALIDA");
                teclado.nextLine();
            }
            opcion = teclado.nextInt();
            teclado.nextLine();
            switch (opcion) {
                case 1:
                    System.out.println("\n\t[RECOMENDACION: Ingresa el nombre en ingles si es posible]");
                    buscarLibro();
                    break;
                case 2:
                    mostrarLibros();
                    break;
                case 3:
                    mostrarAutores();
                    break;
                case 4:
                    autoresVivosPorAnio();
                    break;
                case 5:
                    buscarLibroPorIdioma();
                    break;
                case 6:
                    top10LibrosMasDescargados();
                    break;
                case 7:
                    rankingLibro();
                    break;
                default:
                    System.out.printf("OPCION INVALIDA\n");
            }
        }
    }

    private DatosBusqueda getBusqueda() {
        System.out.println("NOMBRE DEL LIBRO: ");
        var nombreLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "%20"));
        DatosBusqueda datos = conversor.obtenerDatos(json, DatosBusqueda.class);
        return datos;

    }

    private void buscarLibro() {
        DatosBusqueda datosBusqueda = getBusqueda();
        if (datosBusqueda != null && !datosBusqueda.resultado().isEmpty()) {
            DatosLibros primerLibro = datosBusqueda.resultado().get(0);


            Libro libro = new Libro(primerLibro);
            System.out.println("__________LIBRO______");
            System.out.println(libro);
            System.out.println("_____________________");

                Optional<Libro> libroExiste = repositoryLibro.findByTitulo(libro.getTitulo());
                if (libroExiste.isPresent()){
                    System.out.println("\nYA ESTA REGISTRADO !!!\n");
                }else {

                    if (!primerLibro.autor().isEmpty()) {
                        DatosAutor autor = primerLibro.autor().get(0);
                        Autor autor1 = new Autor(autor);
                        Optional<Autor> autorOptional = repositoryAutor.findByNombre(autor1.getNombre());

                        if (autorOptional.isPresent()) {
                            Autor autorExiste = autorOptional.get();
                            libro.setAutor(autorExiste);
                            repositoryLibro.save(libro);
                        } else {
                            Autor autorNuevo = repositoryAutor.save(autor1);
                            libro.setAutor(autorNuevo);
                            repositoryLibro.save(libro);
                        }

                        Integer numeroDescargas = libro.getNumero_descargas() != null ? libro.getNumero_descargas() : 0;
                        System.out.println("----------LIBRO---------------");
                        System.out.printf("TITLE: %s%nAUTHOR: %s%nIDIOMAS: %s%nDESCARGAS: %s%n",
                                libro.getTitulo(), autor1.getNombre(), libro.getLenguaje(), libro.getNumero_descargas());
                        System.out.println("------------------------------n");
                    } else {
                        System.out.println("No se encontro ");
                    }
                }
        } else {
            System.out.println("No se encontro");
        }
    }
    private void mostrarLibros() {
        libros = repositoryLibro.findAll();
        if(libros.isEmpty()){
            System.out.println("No se encontraron libros registrados por el momento...");
        }else {
            libros.stream()
                    .forEach(System.out::println);
        }
    }

    private void mostrarAutores() {
        autores = repositoryAutor.findAll();

        if(autores.isEmpty()){
            System.out.println("No hay autores registrados aun...");
        }else {
            autores.stream()
                    .forEach(System.out::println);
        }
    }

    private void autoresVivosPorAnio() {
        System.out.println("Ingresa el año del autor 'vivo' que desea buscar: ");
        var anio = teclado.nextInt();
        //lista de autores se ibtiene
        autores = repositoryAutor.listaAutoresVivosPorAnio(anio);

        if(autores.isEmpty()) {
            System.out.println("No se encontraron autores vivos...");

        }else{
            autores.stream()
                    .forEach(System.out::println);
        }

    }

    private List<Libro> datosBusquedaLenguaje(String idioma){
        var dato = Idioma.fromString(idioma);
        System.out.println("IDIOMA BUSCADO: " + dato);

        List<Libro> libroPorIdioma = repositoryLibro.findByLenguaje(dato);
        return libroPorIdioma;
    }

    private void buscarLibroPorIdioma(){
        System.out.println("Selecciona el lenguaje/idioma que deseas buscar: ");

        var opcion = -1;
        while (opcion != 0) {
            var opciones = """
                    1. [en] - Ingles
                    2. [es] - Español
                    3. [fr] - Francés
                    4. [pt] - Portugués
                    0. SALIR
                    TU OPCION:
                    """;
            System.out.println(opciones);
            while (!teclado.hasNextInt()) {
                System.out.println("OPCION INVALIDA");
                teclado.nextLine();
            }
            opcion = teclado.nextInt();
            teclado.nextLine();
            switch (opcion) {
                case 1:
                    List<Libro> librosEnIngles = datosBusquedaLenguaje("[en]");
                    if(librosEnIngles.isEmpty()){
                        System.out.println("No hay libros en INGLES\n");
                    }else {
                        librosEnIngles.forEach(System.out::println);
                    }
                    break;
                case 2:
                    List<Libro> librosEnEspanol = datosBusquedaLenguaje("[es]");
                    if(librosEnEspanol.isEmpty()){
                        System.out.println("No hay libros en ESPAÑOL\n");
                    }else {
                        librosEnEspanol.forEach(System.out::println);
                    }
                    break;
                case 3:
                    List<Libro> librosEnFrances = datosBusquedaLenguaje("[fr]");
                    if(librosEnFrances.isEmpty()){
                        System.out.println("No hay libros en FRANCES\n");
                    }else {
                        librosEnFrances.forEach(System.out::println);
                    }
                    break;
                case 4:
                    List<Libro> librosEnPortugues = datosBusquedaLenguaje("[pt]");
                    if(librosEnPortugues.isEmpty()){
                        System.out.println("No hay libros en Frances\n");
                    }else {
                        librosEnPortugues.forEach(System.out::println);
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("No opcion..\n");
            }
        }
    }

    private void top10LibrosMasDescargados() {
        List<Libro> topLibros = repositoryLibro.top10LibrosMasDescargados();
        if(topLibros.isEmpty()){
            System.out.println(":( Aun no tenemos un TOP 10 ");
        }else {
            topLibros.forEach(System.out::println);
        }
    }

    private void rankingLibro() {
        libros = repositoryLibro.findAll();
        IntSummaryStatistics est = libros.stream()
                .filter(l -> l.getNumero_descargas() > 0)
                .collect(Collectors.summarizingInt(Libro::getNumero_descargas));

        Libro libroMasDescargado = libros.stream()
                .filter(l -> l.getNumero_descargas() == est.getMax())
                .findFirst()
                .orElse(null);

        Libro libroMenosDescargado = libros.stream()
                .filter(l -> l.getNumero_descargas() == est.getMin())
                .findFirst()
                .orElse(null);
        System.out.println("________________________ DATOS DE NUESTRO TOP 10  ________________________");
        System.out.printf("%nLibro más descargado: %s%nNúmero de descargas: " +
                "%d%n%nLibro menos descargado: %s%nNúmero de descargas: " +
                "%d%n%n",libroMasDescargado.getTitulo(),est.getMax(),
                libroMenosDescargado.getTitulo(),est.getMin());
        System.out.println("_________________________________________________________________________");
    }

}






