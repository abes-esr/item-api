package fr.abes.item.core.repository.item;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class ILigneFichierExempDaoQueryTest {

    @Test
    void getNbLigneFichierNonTraiteeShouldFilterUntreatedLines() throws NoSuchMethodException {
        assertThat(getNormalizedQueryValue("getNbLigneFichierNonTraitee"))
                .contains("andlf.traitee=0");
    }

    @Test
    void responseCountersShouldOnlyCountProcessedLines() throws NoSuchMethodException {
        assertThat(getNormalizedQueryValue("getNbReponseTrouveesByDemande"))
                .contains("andlf.traitee=1andlf.nbreponse!=0");
        assertThat(getNormalizedQueryValue("getNbUneReponseByDemande"))
                .contains("andlf.traitee=1andlf.nbreponse=1");
        assertThat(getNormalizedQueryValue("getNbZeroReponseByDemande"))
                .contains("andlf.traitee=1andlf.nbreponse=0");
        assertThat(getNormalizedQueryValue("getNbReponseMultipleByDemande"))
                .contains("andlf.traitee=1andlf.nbreponse>1");
    }

    private String getNormalizedQueryValue(String methodName) throws NoSuchMethodException {
        Method method = ILigneFichierExempDao.class.getMethod(methodName, Integer.class);
        Query query = method.getAnnotation(Query.class);
        return query.value().replaceAll("\\s+", "").toLowerCase();
    }
}
