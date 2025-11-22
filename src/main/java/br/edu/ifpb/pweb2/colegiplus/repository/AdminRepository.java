package br.edu.ifpb.pweb2.colegiplus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpb.pweb2.colegiplus.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long>  {
    Admin findByLogin(String login);
}
