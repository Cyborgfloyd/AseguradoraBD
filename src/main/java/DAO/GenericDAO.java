package DAO;

import java.util.List;

public interface GenericDAO<T> {
    void insertar(T t);
    void actualizar(T t);
    void eliminar(int id); // Este contrato exige DELETE físico
    List<T> listar();
    T buscarPorId(int id);
}