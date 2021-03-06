package org.apache.airavata.k8s.api.server.repository.task.type;

import org.apache.airavata.k8s.api.server.model.task.type.TaskModelType;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface TaskTypeRepository extends CrudRepository<TaskModelType, Long> {
    Optional<TaskModelType> findById(long id);
    Optional<TaskModelType> findByName(String name);
}
