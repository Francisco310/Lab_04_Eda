import java.util.*;
import java.io.*;
import java.util.Random;
import java.util.Comparator;

// CLASS PACIENTE
class Paciente{
    private String nombre;
    private String apellido;
    private String id;
    private int categoria;
    private Long tiempodeLLegada;
    private String estado;
    private String area;
    private Stack<String> historialCambios;

    public Paciente(String nombre, String apellido, String id, int categoria, long tiempoLlegada, String estado, String area) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.id = id;
        this.categoria = categoria;
        this.tiempodeLLegada = tiempoLlegada;
        this.estado = estado;
        this.area = area;
        this.historialCambios = new Stack<>();
    }

    public Paciente(){}

    public String getNombre() { return nombre;}
    public String getApellido(){return apellido;}
    public String getId(){return id;}
    public int getCategoria(){return categoria;}
    public Long getTiempodeLLegada(){return tiempodeLLegada;}
    public String getEstado(){return estado;}
    public String getArea(){return area;}
    public Stack<String> getHistorialCambios(){ return historialCambios; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido=apellido; }
    public void setId(String id) { this.id = id; }
    public void setCategoria(int categoria) {this.categoria=categoria;}
    public void setTiempodeLLegada(Long tiempodeLLegada) {this.tiempodeLLegada = tiempodeLLegada;}
    public void setEstado(String estado) {this.estado = estado;}
    public void setArea(String area) {this.area = area;}
    public void setHistorialCambios(Stack<String> historialCambios) {this.historialCambios = historialCambios;}



    public long tiempoEsperaActual(){
        long tiempoActualidad= System.currentTimeMillis()/1000; // Para qu sea en segundos
        return (tiempoActualidad-tiempodeLLegada)/ 60; // Para que sea en minutos
    }

    public void registrarCambio(String descripcion){
        historialCambios.push(descripcion);
    }

    public  String obtenerUltimoCambio(){
        if(historialCambios.isEmpty()){
            return " Sin cambios";
        }else{
            return historialCambios.pop();
        }
    }

}

class ComparadorPaciente implements Comparator<Paciente> {
    public int compare(Paciente p1, Paciente p2) {
        if (p1.getCategoria() == 4 && p2.getCategoria() == 5) return -1;
        if (p1.getCategoria() == 5 && p2.getCategoria() == 4) return 1;
        if (p1.getCategoria() != p2.getCategoria()) {
            return Integer.compare(p1.getCategoria(), p2.getCategoria());
        }
        return Long.compare(p1.getTiempodeLLegada(), p2.getTiempodeLLegada());
    }
}




//CLASS AREAATENCION

class AreaAtencion{
    String nombre ; // Nombre del area
    PriorityQueue<Paciente>pacientesHeap; // cola que mantiene ordenado por nivel urgencia  y tiempo que lleva
    int capacidadMaxima; // Capacidad que pueden manejar simultaneamente

    AreaAtencion(String nombre, int capacidadMaxima){
        this.nombre = nombre;
        this.capacidadMaxima = capacidadMaxima;
        pacientesHeap= new PriorityQueue<>(new ComparadorPaciente());
    }

    String getNombre(){
        return nombre;
    } int getCapacidadMaxima(){
        return capacidadMaxima;
    }

    void setCapacidadMaxima(int capacidadMaxima){ this.capacidadMaxima = capacidadMaxima;}
    void setNombre(String nombre){ nombre=nombre; }



    public void ingresarPaciente(Paciente p){ // Inserta nuevo paciente en el heap
        if(!saturada()){
            pacientesHeap.add(p);
        }
    }

    public boolean saturada(){
        return pacientesHeap.size() >= capacidadMaxima;
    }

    public Paciente atenderPaciente(){ // Obtiene y remueve el paciente con mayor prioridad
        return pacientesHeap.poll();
    }

    public List<Paciente> obtenerPacientesPorHeapSort(){ // Devuelve los pacientes segun prioridad, se s
        PriorityQueue<Paciente>copia = new PriorityQueue<>(pacientesHeap);
        List<Paciente>Orden = new ArrayList<>();
        while(!copia.isEmpty()){
            Orden.add(copia.poll());
        } return Orden;
    }

}

//CLASS HOSPITAL

class Hospital{ //Administra el ingreso,  atencion y seguimiento de pacientes a nivel globlal del hospital
    private Map<String,Paciente>pacientesTotales; //identificador único de cada paciente (ID) con el paciente en el hospital,
    private PriorityQueue<Paciente>colaAtencion; // cola central que gestiona a los pacientes en espera
    private Map<String,AreaAtencion>areasAtencion; // Asignacion de pacientes  a areas especificas
    private List<Paciente>pacientesAtendidos; // Historial de todos los pacientes que han sido atendidos

    Hospital(){
        pacientesTotales = new TreeMap<>();
        colaAtencion = new PriorityQueue<>(new ComparadorPaciente());
        areasAtencion = new TreeMap<>();
        pacientesAtendidos = new ArrayList<>();
    }

    public PriorityQueue<Paciente> getcolaAtencion() {
        return colaAtencion;
    }

    public List<Paciente> getPacientesAtendidos() {
        return pacientesAtendidos;
    }

    public void agregarArea(AreaAtencion area) {
        areasAtencion.put(area.getNombre(), area);
    }

    public Paciente obtenerPacientePorId(String id) {
        return pacientesTotales.get(id);
    }




    public void registrarPaciente(Paciente p){ // asigna su categoría y su área de atención
        pacientesTotales.put(p.getId(),p);
        colaAtencion.add(p);
    }


    public void reasignarCategoria ( String id, int nuevaCategoria){ // permite actualizar la categoría de un paciente y registrar el cambio en su historial.
        Paciente p= pacientesTotales.get(id);
        if(p!=null){
            p.registrarCambio(" Reagsinado de C" + p.getCategoria() + " a C" + nuevaCategoria);
            colaAtencion.remove(p); // eliminar de la cola si es q esta vacia
            p.setCategoria(nuevaCategoria); // actualiza la category
            colaAtencion.add(p); // vyelve a agregar a la cola
        }
    }

    public  Paciente atenderSiguiente(){// extrae de la cola general el paciente con mayor prioridad y lo asigna a su área
        Paciente p= colaAtencion.poll();
        if(p==null){
            return null;
        }
        p.setEstado("atendido");
        pacientesAtendidos.add(p);
        AreaAtencion area= areasAtencion.get(p.getArea());

        if(area!=null){
            area.ingresarPaciente(p);
        }
        return p;
    }



    public List<Paciente> obtenerPacientesPorCategoria(int categoria){
        List<Paciente>x= new ArrayList<>();
        for (Paciente p: colaAtencion){
            if(p.getCategoria()==categoria){
                x.add(p);
            }
        } return x;
    }


    public AreaAtencion obtenerArea(String nombre){
        return areasAtencion.get(nombre);
    }


}

//CLASS GENERATE DATA
class GeneratePacientes {

    private static final String[] nombres = {"Camila", "Juan", "Valentina", "Matías", "Fernanda", "Ignacio", "Sofía", "Benjamín", "Catalina", "Tomás"};
    private static final String[] apellidos = {"González", "Muñoz", "Rojas", "Díaz", "Pérez", "Soto", "Contreras", "Silva", "Martínez", "Sepúlveda"};
    private static final String[] areas = {"SAPU", "urgencia_adulto", "infantil"};

    private static final int totalPacientes = 1440; // 24 horas, cada 10 minutos ( (6*10)*24)= 1440 ( Personas que entran en las 24 horas , considerando que entran al menos 1 persona cad 10 min
    private static final long tiempoInicioDia = 0; // 00:00h por conveniencia

    private Random random = new Random();

    public List<Paciente> generarPacientes(int cantidad) {
        List<Paciente> lista = new ArrayList<>();

        for (int i = 0; i < cantidad; i++) {
            String nombre = nombres[random.nextInt(nombres.length)];
            String apellido = apellidos[random.nextInt(apellidos.length)];
            String id = "P" + (1000 + i); // ID único
            int categoria = generarCategoria();
            long tiempoActual = System.currentTimeMillis() / 1000;
            long tiempoLlegada = i * 600;
            String estado = "en_espera";
            String area = areas[random.nextInt(areas.length)];

            Paciente p = new Paciente(nombre, apellido, id, categoria, tiempoLlegada, estado, area);
            lista.add(p);
        }

        return lista;
    }



    private int generarCategoria() {
        int porcentaje = random.nextInt(100) + 1; // rango [1,100]
//Punto 1.2 simulacion
        if (porcentaje <= 10) return 1;           // C1: 10%
        else if (porcentaje <= 25) return 2;      // C2: 15%
        else if (porcentaje <= 43) return 3;      // C3: 18%
        else if (porcentaje <= 70) return 4;      // C4: 27%
        else return 5;                      // C5: 30%
    }


    public void guardarPacientesEnArchivo(List<Paciente> pacientes, String nombreArchivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo))) {
            for (Paciente p : pacientes) {
                writer.println(p.getId() + "," + p.getNombre() + "," + p.getApellido() + "," +
                        p.getCategoria() + "," + p.getTiempodeLLegada() + "," + p.getEstado() + "," + p.getArea());
            }
            System.out.println("Archivo " + nombreArchivo + " generado correctamente.");
        } catch (IOException e) {
            System.out.println("Error al escribir el archivo: " + e.getMessage());
        }
    }
}

// CLASS SIMLUACION
class Simulacion{ // Clase que simula una jornada completa de 24 horas
    private Hospital hospital;
    private List<Paciente>pacientes;
    private  Map<Integer,List<Long>>tiemposporcategoria= new HashMap<>();
    private List<Paciente>fueraTiempo= new ArrayList<>();
    private int pacientesEsperando=0;


    public Simulacion(Hospital hospital, List<Paciente>pacientes){
        this.hospital = hospital;
        this.pacientes = pacientes;
    }


    private Paciente atenderConPrioridad(Hospital hospital) {
        // Filtramos pacientes C4 antes de C5
        PriorityQueue<Paciente> cola = new PriorityQueue<>(hospital.getcolaAtencion());

        Paciente seleccionado = null;
        for (Paciente paciente : cola) {
            if (paciente.getCategoria() == 4) {
                seleccionado = paciente;
                break;
            }
        }

        if (seleccionado == null) {
            // No hay C4, tomar el siguiente paciente normal
            return hospital.atenderSiguiente();
        } else {
            // Reasignamos la cola sin el paciente seleccionado
            hospital.getcolaAtencion().remove(seleccionado);
            hospital.getPacientesAtendidos().add(seleccionado);
            AreaAtencion area = hospital.obtenerArea(seleccionado.getArea());
            if (area != null) {
                area.ingresarPaciente(seleccionado);
            }
            seleccionado.setEstado("atendido");
            return seleccionado;
        }
    }


    public void simular(int pacientesPorDia) {
        GeneratePacientes generador = new GeneratePacientes(); //esto se deshabilita para usar una lista personalizada
        List<Paciente> pacientes = generador.generarPacientes(pacientesPorDia);

        int minActual = 0;
        int indicePaciente = 0;
        int nuevosIngresos = 0;

        while (minActual < 1440 || indicePaciente < pacientes.size()) {

            // Cada 10 minutos llega un paciente
            if (minActual % 10 == 0 && indicePaciente < pacientes.size()) {
                Paciente p = pacientes.get(indicePaciente++);
                p.setTiempodeLLegada((long) minActual * 60); // tiempo en segundos simulados
                hospital.registrarPaciente(p);
                pacientesEsperando++;
                nuevosIngresos++;

                // Si llegaron 3 nuevos pacientes, atiende 2 de inmediato
                if (nuevosIngresos == 3) {
                    atenderPaciente(minActual);
                    atenderPaciente(minActual);
                    nuevosIngresos = 0;
                }
            }

            // Cada 15 minutos se atiende a un paciente
            if (minActual % 15 == 0) {
                atenderPaciente(minActual);
                pacientesEsperando = 0;
            }

            minActual++;
        }

        Imprimir();
    }



    private void atenderPaciente(int minActual) {
        Paciente p = atenderConPrioridad(hospital);
        if (p != null) {
            long espera = (minActual * 60 - p.getTiempodeLLegada()) / 60;

            // Si no existe la categoría en el mapa, se crea la lista
            if (!tiemposporcategoria.containsKey(p.getCategoria())) {
                tiemposporcategoria.put(p.getCategoria(), new ArrayList<>());
            }

            // Se agrega el tiempo de espera a la lista correspondiente
            tiemposporcategoria.get(p.getCategoria()).add(espera);

            // Verificamos si se pasó del tiempo máximo permitido
            if (pasatiempo(p.getCategoria(), espera)) {
                fueraTiempo.add(p);
            }
        }
    }


    private boolean pasatiempo( int categoria, long esperaMin){
        switch (categoria) {
            case 1: return esperaMin > 0;
            case 2: return esperaMin > 30;
            case 3: return esperaMin > 90;
            case 4: return esperaMin > 180;
            default: return false;
        }
    }


    private void Imprimir() {
        System.out.println("Resumen de simulación:");

        for (int i = 1; i <= 5; i++) {
            List<Long> tiempos = tiemposporcategoria.get(i);
            if (tiempos != null && !tiempos.isEmpty()) {
                long suma = 0;
                for (int j = 0; j < tiempos.size(); j++) {
                    suma += tiempos.get(j);
                }
                double promedio = (double) suma / tiempos.size();
                System.out.println("Categoría C" + i + ": " + tiempos.size() + " pacientes, espera promedio = " + promedio + " minutos");
            } else {
                System.out.println("Categoría C" + i + ": 0 pacientes atendidos.");
            }
        }

        System.out.println("\nPacientes que superaron el tiempo máximo de espera:");
        for (int i = 0; i < fueraTiempo.size(); i++) {
            Paciente p = fueraTiempo.get(i);
            System.out.println("ID: " + p.getId() + ", Categoría: C" + p.getCategoria() + ", Área: " + p.getArea());
        }
    }

}

public class Main {
    public static void main(String[] args) {
        Hospital hospital2 = new Hospital();
        hospital2.agregarArea(new AreaAtencion("SAPU", 50));
        hospital2.agregarArea(new AreaAtencion("urgencia_adulto", 50));
        hospital2.agregarArea(new AreaAtencion("infantil", 50));

        GeneratePacientes g2 = new GeneratePacientes();
        List<Paciente> pacientes2 = g2.generarPacientes(40);

        Simulacion simulacion2 = new Simulacion(hospital2,pacientes2);
        simulacion2.simular(40);

        List<Paciente> atendidos = hospital2.getPacientesAtendidos();

        System.out.println("Tiempos de espera de pacientes C4:");
        for (Paciente p : atendidos) {
            if (p.getCategoria() == 4 && p.getEstado().equals("atendido")) {
                long espera = (p.getTiempodeLLegada()-0) / 60; // puedes ajustar el "0" si tienes otro valor de inicio
                System.out.println("ID: " + p.getId() + ", Espera: " + espera + " minutos");
            }
        }


        Hospital hospital = new Hospital();
        hospital.agregarArea(new AreaAtencion("SAPU", 10));
        hospital.agregarArea(new AreaAtencion("urgencia_adulto", 10));
        hospital.agregarArea(new AreaAtencion("infantil", 10));



        GeneratePacientes g = new GeneratePacientes();

        List<Paciente> pacientes = g.generarPacientes(600);



        Simulacion simulacion = new Simulacion(hospital, pacientes);



        simulacion.simular(600);
        System.out.println();


        Paciente p = hospital.obtenerPacientePorId("P1003");

        if (p != null) {

            p.registrarCambio("Cambio de categoria C" + p.getCategoria() + " a C1");

            hospital.reasignarCategoria("P1003", 1);

            System.out.println("Último cambio registrado para el paciente: "+p.getId() + p.obtenerUltimoCambio());
        }

        if (p != null) {
            System.out.println("Historial de cambios del paciente " + p.getId() + ":");
            for (String cambio : p.getHistorialCambios()) {
                System.out.println("- " + cambio);
            }
        }


    }
}
