package dat.daos;

import dat.entities.Actor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class ActorDAO {

    private final EntityManagerFactory emf;

    public ActorDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Actor create(Actor actor) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(actor);
            em.getTransaction().commit();
            return actor;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
        return actor;
    }

    public Actor findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Actor.class, id);
        } finally {
            em.close();
        }
    }

    public List<Actor> findAll() {
        EntityManager em = emf.createEntityManager();
        return em.createNamedQuery("Actor.findAll", Actor.class).getResultList();
    }

    public List<Actor> findByName(String name) {
        EntityManager em = emf.createEntityManager();
        return em.createNamedQuery("Actor.findByName", Actor.class)
                .setParameter("name", name)
                .getResultList();
    }

    public void delete(Actor actor) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Actor mergedActor = em.merge(actor);
            em.remove(mergedActor);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
    }

}
