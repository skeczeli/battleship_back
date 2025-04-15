package com.example.demo;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Bot {
    private final Set<String> disparosRealizados;
    private final Random random;

    public Bot() {
        this.disparosRealizados = new HashSet<>();
        this.random = new Random();
    }

    public Coordenada disparar() {
        int x, y;
        String clave;

        do {
            x = random.nextInt(10);
            y = random.nextInt(10);
            clave = x + "," + y;
        } while (disparosRealizados.contains(clave));

        disparosRealizados.add(clave);
        return new Coordenada(x, y);
    }

    public static class Coordenada {
        public final int x;
        public final int y;

        public Coordenada(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void reiniciar() {
        disparosRealizados.clear();
    }
}
